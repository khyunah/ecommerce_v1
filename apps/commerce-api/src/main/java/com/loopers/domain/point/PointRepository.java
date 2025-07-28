package com.loopers.domain.point;

import com.loopers.domain.user.vo.UserId;

import java.util.Optional;

public interface PointRepository {
    Optional<Point> findByRefUserId(UserId refUserId);
    Point save(Point point);
}
