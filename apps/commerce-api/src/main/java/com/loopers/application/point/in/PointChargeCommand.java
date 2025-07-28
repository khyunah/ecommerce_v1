package com.loopers.application.point.in;

public record PointChargeCommand(
        String refUserId,
        Long amount
) {

}
