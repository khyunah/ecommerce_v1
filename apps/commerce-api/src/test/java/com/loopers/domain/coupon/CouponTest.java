package com.loopers.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouponTest {

    @DisplayName("발급된 쿠폰은 한번만 사용할 수 있다.")
    @Test
    void should_apply_fixed_amount_coupon_and_return_discounted_price(){
        // given
        Coupon coupon = Coupon.from(1L, "티셔츠 할인쿠폰", CouponType.PRICE.name(),10000L,0);
        coupon.useCoupon();

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            coupon.useCoupon();
        });

        // than
        assertThat(exception.getMessage()).isEqualTo("이미 사용한 쿠폰입니다.");
    }
}
