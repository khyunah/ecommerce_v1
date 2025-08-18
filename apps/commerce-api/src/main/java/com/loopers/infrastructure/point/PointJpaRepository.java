package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point,Long> {
    Optional<Point> findByRefUserId(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Point p where p.refUserId = :refUserId")
    @QueryHints(value = {@QueryHint(name = "javax.persistence.lock.timeout", value = "5000")})
    Optional<Point> findByRefUserIdWithLock(@Param("refUserId") Long refUserId);
}
