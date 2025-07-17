package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    public Point charge(Point point, int amount){
        Point getPoint = pointRepository.findByUserId(point.getUserId()).orElse(null);
        if(null == getPoint){
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저 ID 로 충전을 시도했습니다.");
        }

        Point charged = Point.charge(getPoint, amount);
        return pointRepository.save(charged);
    }
}
