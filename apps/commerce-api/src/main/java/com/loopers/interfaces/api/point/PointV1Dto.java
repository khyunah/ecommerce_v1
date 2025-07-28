package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;

public class PointV1Dto {
    public record PointChargeRequest(
            Long refUserId,
            int amount
    ){}
    public record PointInfoResponse(
            Long refUserId,
            int point
    ){
        public static PointInfoResponse from(PointInfo pointInfo) {
            return new PointInfoResponse(
                    pointInfo.refUserId(),
                    pointInfo.point()
            );
        }
    }
}
