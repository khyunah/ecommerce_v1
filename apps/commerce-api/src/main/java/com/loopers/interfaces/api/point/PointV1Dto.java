package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;

public class PointV1Dto {
    public record PointChargeRequest(
            String userId,
            int amount
    ){}
    public record PointInfoResponse(
            String userId,
            int point
    ){
        public static PointInfoResponse from(PointInfo pointInfo) {
            return new PointInfoResponse(
                    pointInfo.userId(),
                    pointInfo.point()
            );
        }
    }
}
