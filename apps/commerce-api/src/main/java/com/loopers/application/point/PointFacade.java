package com.loopers.application.point;

import com.loopers.application.point.in.PointChargeCommand;
import com.loopers.application.point.out.PointInfo;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final PointService pointService;

    public PointInfo get(String refUserId){
        Point point = pointService.get(refUserId);
        return PointInfo.from(point);
    }

    public PointInfo charge(PointChargeCommand command){
        Point point = pointService.get(command.refUserId());
        Point user = pointService.charge(point, command.amount());
        return PointInfo.from(user);
    }
}
