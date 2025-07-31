package com.loopers.domain.like;

public interface LikeRepository {
    Like save(Like like);
    boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
    boolean delete(Long refUserId, Long refProductId);
}
