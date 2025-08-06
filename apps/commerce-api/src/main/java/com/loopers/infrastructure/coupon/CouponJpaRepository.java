package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon,Long> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select c from Coupon c where c.id = :id and c.refUserId = :refUserId")
    Optional<Coupon> findByIdAndRefUserIdWithLock(Long id, Long refUserId);
}
