package com.loopers.application.order;

import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.application.order.out.OrderCreateResult;
import com.loopers.application.order.out.OrderDetailResult;
import com.loopers.application.order.out.OrderResult;
import com.loopers.application.payment.PaymentStatusService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.order.event.CouponUsageEvent;
import com.loopers.domain.order.event.OrderCompletedEvent;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 Facade - 쿠폰 처리를 이벤트로 분리하되 트랜잭션 유지
 * - 쿠폰 처리: 이벤트로 분리 (관심사 분리)
 * - 트랜잭션: 동기 처리로 원자성 보장
 * - 쿠폰 실패 시 전체 주문 롤백
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final ApplicationEventPublisher eventPublisher;
    private final CouponService couponService;

    private final UserService userService;
    private final StockService stockService;
    private final PointService pointService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentStatusService paymentStatusService;

    @Transactional
    public OrderCreateResult placeOrder(OrderCreateCommand command) {
        log.info("주문 처리 시작 - userId: {}, couponId: {}", command.userId(), command.couponId());

        User user = userService.get(command.userId());
        Order order = null;
        Long totalPrice = 0L;
        Long usedPoint = command.usedPoint();

        try {
            // === 하나의 트랜잭션에서 처리 (원자성 보장) ===
            
            // 1. 재고 처리
            for (OrderItemCriteria item : command.items()) {
                Stock stock = stockService.getByRefProductIdWithLock(item.productId());
                stockService.updateQuantity(stock, item.quantity());
                log.debug("재고 업데이트 완료 - productId: {}, 남은 수량: {}", item.productId(), stock.getQuantity());
            }

            List<OrderItem> orderItems = new ArrayList<>();

            // 2. 주문 상품 존재 확인 및 주문아이템 추가
            for (OrderItemCriteria item : command.items()) {
                Product product = productService.getDetail(item.productId());
                totalPrice += product.getSellingPrice().getValue().longValue() * item.quantity();
                orderItems.add(OrderItem.create(
                        product.getId(),
                        item.quantity(),
                        product.getName(),
                        product.getSellingPrice(),
                        product.getOriginalPrice()
                ));
            }

            // 3. 쿠폰 적용 (이벤트로 분리하되 동기 처리)
            Long discountAmount = 0L;
            if (command.couponId() != null && command.couponId() > 0) {
                discountAmount = processCouponWithEvent(command.userId(), command.couponId(), totalPrice);
            }

            // 4. 포인트 차감
            Point point = pointService.getByRefUserIdWithLock(command.userId());
            Point.minus(point, usedPoint);
            pointService.save(point);
            log.info("포인트 차감 완료 - userId: {}, 사용 포인트: {}", command.userId(), usedPoint);

            // 5. 주문 생성 (쿠폰 할인이 적용된 상태로)
            order = Order.create(user.getId(), command.orderSeq(), orderItems, totalPrice);
            order.applyPoint(usedPoint);
            if (discountAmount > 0) {
                order.applyCoupon(command.couponId(), discountAmount);
            }
            order = orderService.save(order);
            log.info("주문 생성 완료 - orderId: {}, 최종 금액: {}", order.getId(), order.getFinalAmount());

            // 6. 결제 처리
            handlePayment(order, command);

            // 7. 주문 완료 이벤트 발행 (외부 시스템 전송을 이벤트로 분리)
            publishOrderCompletedEvent(order, command);

        } catch (Exception e){
            log.error("주문 처리 중 오류 발생 - userId: {}", command.userId(), e);
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 중 에러가 발생했습니다. 다시 시도해주세요.");
        }

        return OrderCreateResult.from(order, totalPrice);
    }

    /**
     * 주문 완료 이벤트 발행 - 외부 시스템 전송과 분리
     */
    private void publishOrderCompletedEvent(Order order, OrderCreateCommand command) {
        try {
            log.info("주문 완료 이벤트 발행 - orderId: {}, userId: {}", order.getId(), order.getRefUserId());
            
            OrderCompletedEvent event = OrderCompletedEvent.create(
                    order.getId(),
                    order.getRefUserId(),
                    order.getTotalPrice(),
                    order.getFinalAmount(),
                    command.paymentMethod()
            );
            
            eventPublisher.publishEvent(event);
            
        } catch (Exception e) {
            // 이벤트 발행 실패해도 주문 처리는 성공으로 처리
            log.error("주문 완료 이벤트 발행 실패 - orderId: {}", order.getId(), e);
        }
    }

    // 쿠폰 이벤트
    private Long processCouponWithEvent(Long userId, Long couponId, Long totalPrice) {
        try {
            CouponUsageEvent event = CouponUsageEvent.create(userId, couponId, totalPrice);
            eventPublisher.publishEvent(event);
            return event.getTotalOrderAmount();
            
        } catch (Exception e) {
            log.error("쿠폰 사용 실패 - userId: {}, couponId: {}", userId, couponId, e);
            throw e;
        }
    }

    /**
     * 결제 처리
     */
    private void handlePayment(Order order, OrderCreateCommand command) {
        Long finalAmount = order.getFinalAmount();
        Payment payment = paymentService.createPayment(
                order.getId(),
                command.paymentMethod(),
                finalAmount,
                command.pgProvider()
        );

        PaymentMethod paymentMethod = PaymentMethod.from(command.paymentMethod());
        
        if (paymentMethod.requiresPgConnection() && finalAmount > 0) {
            handlePgPayment(payment, command);
        } else {
            order.completePayment();
            orderService.save(order);
            log.info("결제 완료 (PG사 연결 없음) - orderId: {}", order.getId());
        }
    }

    /**
     * PG 결제 처리
     */
    private void handlePgPayment(Payment payment, OrderCreateCommand command) {
        try {
            var pgResponse = paymentService.requestPgPayment(
                    payment, 
                    command.cardType(), 
                    command.cardNo()
            );
            
            if (pgResponse.isSuccess()) {
                paymentService.updateTransactionKey(payment.getPaymentSeq(), pgResponse.getTransactionKey());
                log.info("PG 요청 성공 - transactionKey: {}", pgResponse.getTransactionKey());
            } else {
                String errorCode = pgResponse.meta().errorCode();
                String errorMessage = pgResponse.getErrorMessage();
                
                paymentService.failPayment(payment.getPaymentSeq(), errorMessage);
                
                if ("Circuit Breaker Open".equals(errorCode) || "Timeout".equals(errorCode)) {
                    payment.updateStatus(PaymentStatus.TIMEOUT_PENDING);
                    paymentService.save(payment);
                    startAsyncPaymentStatusCheck(payment);
                    log.info("PG 타임아웃/Circuit Breaker - 결제 확인 중");
                } else {
                    throw new CoreException(ErrorType.BAD_REQUEST, "결제 요청에 실패했습니다: " + errorMessage);
                }
            }
        } catch (Exception e) {
            paymentService.failPayment(payment.getPaymentSeq(), e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 처리 중 시스템 오류가 발생했습니다.");
        }
    }

    public List<OrderResult> getOrders(Long refUserId) {
        List<Order> orders = orderService.getOrders(refUserId);
        return OrderResult.from(orders);
    }

    public OrderDetailResult getOrderDetail(Long orderId, Long userId) {
        Order order = orderService.getOrderDetail(orderId, userId);
        return OrderDetailResult.from(order);
    }

    @Async
    public void startAsyncPaymentStatusCheck(Payment payment) {
        try {
            log.info("비동기 결제 상태 확인 시작: {}", payment.getPaymentSeq());
            paymentStatusService.checkAndRecoverPaymentStatus(payment);
        } catch (Exception e) {
            log.error("비동기 결제 상태 확인 실패: {}", payment.getPaymentSeq(), e);
        }
    }
}
