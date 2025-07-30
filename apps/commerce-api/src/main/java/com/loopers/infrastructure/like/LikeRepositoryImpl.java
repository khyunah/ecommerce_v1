package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public boolean save(Long refUserId, Long refProductId) {
        return likeJpaRepository.save(refUserId, refProductId);
    }

    @Override
    public boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId) {
        return likeJpaRepository.existsByRefUserIdAndRefProductId(refUserId, refProductId);
    }

    @Override
    public boolean delete(Long refUserId, Long refProductId) {
        return likeJpaRepository.deleteByRefUserIdAndRefProductId(refUserId, refProductId);
    }

}
