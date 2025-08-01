package com.loopers.domain.point;

import com.loopers.domain.user.vo.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointService {

    private final PointRepository pointRepository;

    public Point get(String refUserId){
        return pointRepository.findByRefUserId(UserId.from(refUserId))
                .orElseThrow(()-> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저 ID 입니다."));
    }

    public Point charge(Point point, Long amount){
        Point findedPoint = pointRepository.findByRefUserId(point.getRefUserId())
                .orElseThrow(()-> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저 ID 로 충전을 시도했습니다."));
        Point charged = Point.charge(findedPoint, amount);
        return pointRepository.save(charged);
    }
}
