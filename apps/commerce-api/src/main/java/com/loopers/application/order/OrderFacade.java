package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import com.loopers.domain.point.vo.Balance;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.stock.StockService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Order placeOrder(Long userId, List<OrderItemResult> items, Long userPointsToUse,String orderSeq) {

        // 사용자 정보
        User user = userService.get(userId);

        // 재고처리
        for (OrderItemResult item : items) {
            Stock stock = stockService.getByRefProductId(item.productId());
            stock.updateQuantity(stock.getQuantity(), item.quantity());
        }

        // 포인트 차감
        Point point = pointService.getByRefUserId(userId);
        Point.minus(point, userPointsToUse);
        pointService.save(point);

        // 주문 상품 존재 확인
        List<OrderItem> orderItems = items.stream()
                .map(req -> {
                    Product product = productService.getDetail(req.productId());
                    return OrderItem.create(
                            product.getId(),
                            req.quantity(),
                            product.getName(),
                            product.getSellingPrice(),
                            product.getOriginalPrice());
                }).toList();

        // 주문 생성
        Order order = Order.create(user.getId(), orderItems);
        order = orderService.save(order);

        externalOrderSender.sendOrder(order);
        return order;
    }

    public List<OrderSummaryResult> getOrders(Long refUserId) {
        List<Order> orders = orderService.findAllByUserId(refUserId);

        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream().map(item ->
                        new OrderSummaryResult(
                                order.getId(),
                                order.getOrderStatus().name(),
                                order.getCreatedAt().toLocalDateTime(),
                                item.getSellingPrice().getValue(),
                                item.getProductId()
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
