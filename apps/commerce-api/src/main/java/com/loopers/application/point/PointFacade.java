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

    public PointInfo get(String userId){
        Point getUser = pointService.get(userId);
        if(null == getUser){
            return null;
        }
        return PointInfo.from(getUser);
    }

    public PointInfo charge(PointV1Dto.PointChargeRequest pointChargeRequest){
        Point getPoint = pointService.get(pointChargeRequest.userId());
        if(null == getPoint){
            return null;
        }
        Point point = new Point(pointChargeRequest.userId());
        Point getUser = pointService.charge(point, pointChargeRequest.amount());
        return PointInfo.from(getUser);
    }
}
