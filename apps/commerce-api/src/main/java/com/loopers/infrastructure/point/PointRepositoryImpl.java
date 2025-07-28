package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    @Override
    public Optional<Point> findByRefUserId(UserId refUserId) {
        return pointJpaRepository.findByRefUserId(refUserId);
    }

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }

}
