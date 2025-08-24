package com.loopers.application.order.in;

import java.util.List;

public record OrderCreateCommand(
        Long userId,
        List<OrderItemCriteria> items,
        String orderSeq,
        Long couponId,
        Long usedPoint,
        String paymentMethod,
        String pgProvider,
        String cardType,    // 카드 타입 (SAMSUNG, HYUNDAI, etc.)
        String cardNo       // 카드 번호
) {
}
