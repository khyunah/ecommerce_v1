package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.interfaces.api.point.PointV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final PointService pointService;

    public PointInfo get(Long refUserId){
        Point point = pointService.get(refUserId);
        if(null == point){
            return null;
        }
        return PointInfo.from(point);
    }

    public PointInfo charge(PointV1Dto.PointChargeRequest pointChargeRequest){
        pointService.get(pointChargeRequest.refUserId());
        Point point = new Point(pointChargeRequest.refUserId());
        Point user = pointService.charge(point, pointChargeRequest.amount());
        return PointInfo.from(user);
    }
}
