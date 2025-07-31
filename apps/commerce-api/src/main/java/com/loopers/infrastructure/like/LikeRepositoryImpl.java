package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public Like save(Like like) {
        return likeJpaRepository.save(like);
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
