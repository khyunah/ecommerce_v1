package com.loopers.application.point;

import com.loopers.domain.point.Point;

public record PointInfo(
        Long refUserId,
        int point
) {
    public static PointInfo from(Point point) {
        return new PointInfo(
                point.getRefUserId(),
                point.getPoint()
        );
    }
}
