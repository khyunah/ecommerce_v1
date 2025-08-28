package com.loopers.domain.order;

public interface ExternalOrderSender {
    void sendOrder(Order order);
    void sendOrderCancellation(Order order, String reason, String message);
}
