package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.user.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point,Long> {
    Optional<Point> findByRefUserId(UserId userId);
}
