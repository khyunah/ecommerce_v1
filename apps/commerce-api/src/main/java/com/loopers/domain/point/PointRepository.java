package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<Point> findByUserId(String userId);
    Point save(Point point);
    boolean existsByUserId(String userId);
}
