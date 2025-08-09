package com.loopers.application.order.out;

import com.loopers.domain.order.Order;

import java.time.LocalDateTime;

public record OrderCreateResult(
        Long orderId,
        String status,
        LocalDateTime orderedAt,
        Long price
) {
    public static OrderCreateResult from(Order order, Long totalPrice) {
        return new OrderCreateResult(
              order.getId(),
              order.getOrderStatus().name(),
              order.getCreatedAt().toLocalDateTime(),
              totalPrice
        );
    }
}
