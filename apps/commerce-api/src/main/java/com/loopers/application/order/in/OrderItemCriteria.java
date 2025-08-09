package com.loopers.application.order.in;

public record OrderItemCriteria(
        Long productId,
        int quantity
) {

}
