package com.loopers.application.order;

import com.loopers.domain.order.Order;

import java.time.LocalDateTime;

public record OrderSummaryResult(
        Long orderId,
        String status,
        LocalDateTime orderedAt,
        Long price
) {
    public static OrderSummaryResult from(Order order, Long totalPrice) {
        return new OrderSummaryResult(
              order.getId(),
              order.getOrderStatus().name(),
              order.getCreatedAt().toLocalDateTime(),
              totalPrice
        );
    }
}
