package com.loopers.domain.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CouponUsageEvent {
    private final Long userId;
    private final Long couponId;
    private final Long totalOrderAmount;
    private Long discountAmount;
    private final LocalDateTime occurredAt;

    public CouponUsageEvent(Long userId, Long couponId, Long totalOrderAmount) {
        this.userId = userId;
        this.couponId = couponId;
        this.totalOrderAmount = totalOrderAmount;
        this.discountAmount = 0L;
        this.occurredAt = LocalDateTime.now();
    }

    public static CouponUsageEvent create(Long userId, Long couponId, Long totalOrderAmount) {
        return new CouponUsageEvent(userId, couponId, totalOrderAmount);
    }

    public void updateDiscountAmount(Long discountAmount) {
        this.discountAmount = discountAmount;
    }
}
