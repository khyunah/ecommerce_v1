package com.loopers.domain.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointService {

    private final PointRepository pointRepository;

    public Point get(String userId){
        Point point = pointRepository.findByUserId(userId).orElse(null);
        if(null == point){
            return null;
        }
        return point;
    }

}
