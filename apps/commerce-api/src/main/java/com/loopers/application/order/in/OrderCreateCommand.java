package com.loopers.application.order.in;

import java.util.List;

public record OrderCreateCommand(
        Long userId,
        List<OrderItemCriteria> items,
        String orderSeq,
        Long couponId,
        Long usedPoint
) {
}
