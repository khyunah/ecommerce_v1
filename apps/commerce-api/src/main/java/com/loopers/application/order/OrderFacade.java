package com.loopers.application.order;

import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
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

    @Transactional
    public OrderSummaryResult placeOrder(OrderCreateCommand command) {

        // 사용자 정보
        User user = userService.get(command.userId());
        Order order = null;
        long totalPrice = 0L;

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
            if(command.couponId() > -1){
                Coupon coupon = couponService.get(command.couponId(), command.userId());
                coupon.useCoupon();
            }

            // 포인트 차감
            Point point = pointService.getByRefUserIdWithLock(command.userId());
            System.out.println("point 확인: " + point.getRefUserId());
            Point.minus(point, totalPrice);
            pointService.save(point);

            // 주문 생성
            order = Order.create(user.getId(), command.orderSeq(), orderItems);
            order = orderService.save(order);
            System.out.println("order 확인: " + order.getOrderStatus());

        } catch (Exception e){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 중 에러가 발생했습니다. 다시 시도해주세요.");
        }
        OrderSummaryResult result = null;
        if(order != null){
            result = OrderSummaryResult.from(order, totalPrice);
            externalOrderSender.sendOrder(order);
        }
        return result;
    }

    public List<OrderSummaryResult> getOrders(Long refUserId) {
        List<Order> orders = orderService.findAllByUserId(refUserId);

        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream().map(item ->
                        new OrderSummaryResult(
                                order.getId(),
                                order.getOrderStatus().name(),
                                order.getCreatedAt().toLocalDateTime(),
                                item.getSellingPrice().getValue().longValue()
                        )
                ))
                .toList();
    }

    public OrderDetailResult getOrderDetail(Long userId, Long orderId) {
        Order order = orderService.findByIdAndUserId(orderId, userId);

        List<OrderDetailResult.OrderItemDetail> items = order.getOrderItems().stream()
                .map(item -> new OrderDetailResult.OrderItemDetail(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getOriginalPrice().getValue(),
                        item.getSellingPrice().getValue()
                ))
                .toList();

        return new OrderDetailResult(order.getId(), order.getOrderStatus().name(), items);
    }
}
