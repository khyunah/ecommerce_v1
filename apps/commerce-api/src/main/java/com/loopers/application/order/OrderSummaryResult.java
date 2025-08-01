package com.loopers.application.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResult(
        Long orderId,
        String status,
        LocalDateTime orderedAt,
        BigDecimal price,
        Long productId
) {
}
