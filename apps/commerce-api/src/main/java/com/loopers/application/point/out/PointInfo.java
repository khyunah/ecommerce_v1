package com.loopers.application.point.out;

import com.loopers.domain.point.Point;

public record PointInfo(
        String refUserId,
        Long balance
) {
    public static PointInfo from(Point point) {
        return new PointInfo(
                point.getRefUserId().getValue(),
                point.getBalance().getValue()
        );
    }
}
