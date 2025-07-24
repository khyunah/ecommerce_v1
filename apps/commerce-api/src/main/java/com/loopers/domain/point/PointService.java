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
        return pointRepository.findByUserId(userId).orElse(null);
    }

    public Point charge(Point point, int amount){
        Point findedPoint = pointRepository.findByUserId(point.getUserId()).orElse(null);
        if(null == findedPoint){
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저 ID 로 충전을 시도했습니다.");
        }

        Point charged = Point.charge(findedPoint, amount);
        return pointRepository.save(charged);
    }
}
