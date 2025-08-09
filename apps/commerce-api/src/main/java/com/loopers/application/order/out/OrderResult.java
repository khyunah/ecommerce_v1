package com.loopers.application.order.out;

import com.loopers.domain.order.Order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
        Long orderId,
        String status,
        LocalDateTime orderedAt,
        Long price
) {
    public static List<OrderResult> from(List<Order> orders){
        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream().map(item ->
                        new OrderResult(
                                order.getId(),
                                order.getOrderStatus().name(),
                                order.getCreatedAt().toLocalDateTime(),
                                item.getSellingPrice().getValue().longValue()
                        )
                ))
                .toList();
    }
}
