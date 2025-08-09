package com.loopers.interfaces.api.point;

import com.loopers.application.point.in.PointChargeCommand;
import com.loopers.application.point.out.PointInfo;

public class PointV1Dto {
    public record PointChargeRequest(
            Long refUserId,
            Long amount
    ){
        public static PointChargeCommand toCommand(PointV1Dto.PointChargeRequest request) {
            return new PointChargeCommand(
                    request.refUserId,
                    request.amount
            );
        }
    }
    public record PointInfoResponse(
            Long refUserId,
            Long balance
    ){
        public static PointInfoResponse from(PointInfo pointInfo) {
            return new PointInfoResponse(
                    pointInfo.refUserId(),
                    pointInfo.balance()
            );
        }
    }
}
