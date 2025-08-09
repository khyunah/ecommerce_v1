package com.loopers.application.point.in;

public record PointChargeCommand(
        Long refUserId,
        Long amount
) {

}
