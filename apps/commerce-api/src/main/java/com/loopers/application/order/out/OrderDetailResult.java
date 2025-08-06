package com.loopers.application.order.out;

import java.math.BigDecimal;
import java.util.List;

public record OrderDetailResult(
        Long orderId,
        String orderStatus,
        List<OrderItemDetail> items
) {
    public record OrderItemDetail(
            String productName,
            int quantity,
            BigDecimal originalPrice,
            BigDecimal discountedPrice
    ) {}
}
