package com.loopers.application.order;

import com.loopers.domain.order.ExternalOrderSender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.vo.Balance;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.vo.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final ExternalOrderSender externalOrderSender;

    @Transactional
    public Order placeOrder(String userIdLong, List<OrderItemResult> items, int userPointsToUse) {
        UserId userId = UserId.from(String.valueOf(userIdLong));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "사용자가 존재하지 않습니다."));

        for (OrderItemResult item : items) {
            Stock stock = stockRepository.findByRefProductId(item.productId())
                    .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "상품 재고가 존재하지 않습니다: " + item.productId()));
            if (stock.getQuantity() < item.quantity()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다: " + item.productId());
            }
            stockRepository.save(new Stock(stock.getRefProductId(), stock.getQuantity() - item.quantity()));
        }

        Point point = pointRepository.findByRefUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "포인트 정보가 존재하지 않습니다."));
        if (point.getBalance().getValue() < userPointsToUse) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        Balance deducted = Balance.minus(point, userPointsToUse);
        Point updatedPoint = new Point(userId, deducted);
        pointRepository.save(updatedPoint);

        List<OrderItem> orderItems = items.stream()
                .map(req -> {
                    Product product = productRepository.findById(req.productId())
                            .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "상품이 존재하지 않습니다: " + req.productId()));
                    return OrderItem.create(
                            product.getId(),
                            req.quantity(),
                            product.getName(),
                            product.getSellingPrice(),
                            product.getOriginalPrice());
                }).toList();

        Order order = Order.create(user.getId(), orderItems);
        order = orderRepository.save(order); // orderItems 는 cascade로 저장됨

        externalOrderSender.sendOrder(order);
        return order;
    }

    public List<OrderSummaryResult> getOrders(Long refUserId) {
        List<Order> orders = orderRepository.findAllByUserId(refUserId);

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
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));

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
