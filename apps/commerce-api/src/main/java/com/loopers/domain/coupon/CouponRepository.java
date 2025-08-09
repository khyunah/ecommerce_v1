package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findById(Long id);
    Optional<Coupon> findByIdAndRefUserIdWithLock(Long id, Long refUserId);
    Coupon save(Coupon coupon);
}
