package com.loopers.application.point;

import com.loopers.domain.point.Point;

public record PointInfo(
        String userId,
        int point
) {
    public static PointInfo from(Point point) {
        return new PointInfo(
                point.getUserId(),
                point.getPoint()
        );
    }
}
