package com.loopers.application.order;

public record OrderItemResult(
        Long productId,
        int quantity
) {
}
