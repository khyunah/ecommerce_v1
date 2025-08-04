package com.loopers.domain.coupon;

import com.loopers.domain.user.vo.Gender;

import java.util.Arrays;

public enum CouponType {
    RATE, PRICE;

    public static CouponType from(String value) {
        validate(value);
        return CouponType.valueOf(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("쿠폰타입은 비어있을 수 없습니다.");
        } else {
            boolean exists = Arrays.stream(Gender.values())
                    .anyMatch(g -> g.name().equalsIgnoreCase(value));

            if (!exists) {
                throw new IllegalArgumentException("쿠폰타입은 RATE 이거나 PRICE 이어야 합니다.");
            }
        }

    }
}
