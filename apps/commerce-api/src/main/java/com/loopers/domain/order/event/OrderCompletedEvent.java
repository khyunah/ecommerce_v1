package com.loopers.domain.order.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderCompletedEvent {
    private final Long orderId;
    private final Long userId;
    private final Long totalAmount;
    private final Long finalAmount;
    private final String paymentMethod;
    private final LocalDateTime occurredAt;

    public OrderCompletedEvent(Long orderId, Long userId, Long totalAmount, 
                              Long finalAmount, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * 주문 완료 이벤트 생성
     */
    public static OrderCompletedEvent create(Long orderId, Long userId, Long totalAmount,
                                           Long finalAmount, String paymentMethod) {
        return new OrderCompletedEvent(orderId, userId, totalAmount, finalAmount, paymentMethod);
    }
}
