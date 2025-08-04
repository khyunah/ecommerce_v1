package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<Point> findByRefUserId(Long refUserId);
    Point save(Point point);
    void deduct(Long userId, long amount);
}
