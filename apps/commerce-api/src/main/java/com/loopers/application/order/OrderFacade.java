package com.loopers.application.order;

import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.application.order.out.OrderCreateResult;
import com.loopers.application.order.out.OrderDetailResult;
import com.loopers.application.order.out.OrderResult;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final ExternalOrderSender externalOrderSender;

    private final UserService userService;
    private final StockService stockService;
    private final PointService pointService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final PaymentService paymentService;

    @Transactional
    public OrderCreateResult placeOrder(OrderCreateCommand command) {

        // 사용자 정보
        User user = userService.get(command.userId());
        Order order = null;
        Long totalPrice = 0L;
        Long usedPoint = command.usedPoint();

        try {
            // 재고처리
            for (OrderItemCriteria item : command.items()) {
                Stock stock = stockService.getByRefProductIdWithLock(item.productId());
                stockService.updateQuantity(stock, item.quantity());
                System.out.println("stock 확인: " + stock.getQuantity());
            }

            List<OrderItem> orderItems = new ArrayList<>();

            // 주문 상품 존재 확인 및 주문아이템 추가
            for (OrderItemCriteria item : command.items()) {
                Product product = productService.getDetail(item.productId());
                System.out.println("product 확인: " + product.getName());
                totalPrice += product.getSellingPrice().getValue().longValue() * item.quantity();
                System.out.println("product 할인가격: " + product.getSellingPrice().getValue().longValue());
                System.out.println("product 원가격: " + product.getOriginalPrice().getValue().longValue());
                orderItems.add(OrderItem.create(
                        product.getId(),
                        item.quantity(),
                        product.getName(),
                        product.getSellingPrice(),
                        product.getOriginalPrice()
                ));
            }

            // 쿠폰 적용
            Coupon coupon = null;
            Long discountAmount = 0L;
            if(command.couponId() > -1){
                coupon = couponService.get(command.couponId(), command.userId());
                coupon.useCoupon();
                discountAmount = coupon.applyCoupon(coupon, totalPrice);
            }

            // 포인트 차감
            Point point = pointService.getByRefUserIdWithLock(command.userId());
            System.out.println("point 확인: " + point.getRefUserId());
            Point.minus(point, usedPoint);
            pointService.save(point);

            // 주문 생성
            order = Order.create(user.getId(), command.orderSeq(), orderItems, totalPrice);
            order.applyPoint(usedPoint);
            if(coupon != null){
                order.applyCoupon(coupon.getId(), discountAmount);
            }
            order = orderService.save(order);
            System.out.println("order 확인: " + order.getOrderStatus());

            // 결제 처리
            Long finalAmount = order.getFinalAmount();
            Payment payment = null;
            
            // 항상 결제 생성 (금액이 0원이어도)
            payment = paymentService.createPayment(
                    order.getId(),
                    command.paymentMethod(),
                    finalAmount,
                    command.pgProvider()
            );
            System.out.println("payment 생성: " + payment.getPaymentSeq());

            PaymentMethod paymentMethod = PaymentMethod.from(command.paymentMethod());
            
            // CARD 결제 방법이고 금액이 0보다 클 때만 PG사 연결
            if (paymentMethod.requiresPgConnection() && finalAmount > 0) {
                // PG사 결제 요청
                try {
                    var pgResponse = paymentService.requestPgPayment(
                            payment, 
                            command.cardType(), 
                            command.cardNo()
                    );
                    
                    if (pgResponse.isSuccess()) {
                        // PG 요청 성공 - transactionKey 저장
                        paymentService.updateTransactionKey(payment.getPaymentSeq(), pgResponse.getTransactionKey());
                        System.out.println("PG 요청 성공, transactionKey: " + pgResponse.getTransactionKey());
                        
                        // 주문 상태는 PENDING으로 유지 (실제 결제 완료는 콜백에서 처리)
                        // order 상태는 이미 PENDING이므로 별도 처리 불필요
                        
                    } else {
                        // PG 요청 실패 - errorCode에 따라 다르게 처리
                        String errorCode = pgResponse.meta().errorCode();
                        String errorMessage = pgResponse.getErrorMessage();
                        
                        paymentService.failPayment(payment.getPaymentSeq(), errorMessage);
                        
                        if ("Bad Request".equals(errorCode)) {
                            // 클라이언트 요청 오류 (400번대)
                            throw new CoreException(ErrorType.BAD_REQUEST, errorMessage);
                        } else if ("Internal Server Error".equals(errorCode)) {
                            // PG사 서버 오류 (500번대) - 재시도 가능한 오류로 처리
                            throw new CoreException(ErrorType.INTERNAL_ERROR, errorMessage);
                        } else {
                            // 기타 오류
                            throw new CoreException(ErrorType.BAD_REQUEST, "결제 요청에 실패했습니다: " + errorMessage);
                        }
                    }
                } catch (CoreException e) {
                    // CoreException은 그대로 재전파
                    throw e;
                } catch (Exception e) {
                    // 네트워크 오류 등 예상치 못한 예외
                    paymentService.failPayment(payment.getPaymentSeq(), e.getMessage());
                    throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 처리 중 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                }
            } else {
                // CARD가 아니거나 금액이 0원인 경우 바로 주문 완료
                order.completePayment();
                order = orderService.save(order);
                System.out.println("결제 완료 (PG사 연결 없음): " + order.getOrderStatus());
            }

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("OrderFacade placeOrder 에러: " + e.getMessage());
            System.out.println("OrderFacade placeOrder 스택 트레이스:");
            e.printStackTrace();
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 중 에러가 발생했습니다. 다시 시도해주세요.");
        }
        OrderCreateResult result = null;
        if(order != null){
            result = OrderCreateResult.from(order, totalPrice);
            externalOrderSender.sendOrder(order);
        }
        return result;
    }

    public List<OrderResult> getOrders(Long refUserId) {
        List<Order> orders = orderService.getOrders(refUserId);
        return OrderResult.from(orders);
    }

    public OrderDetailResult getOrderDetail(Long orderId, Long userId) {
        Order order = orderService.getOrderDetail(orderId, userId);
        return OrderDetailResult.from(order);
    }
}
