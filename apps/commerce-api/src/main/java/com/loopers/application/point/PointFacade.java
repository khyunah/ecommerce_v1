package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
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

}
