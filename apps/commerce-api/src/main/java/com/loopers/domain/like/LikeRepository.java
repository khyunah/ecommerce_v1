package com.loopers.domain.like;

public interface LikeRepository {
    boolean save(Long refUserId, Long refProductId);
    boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
}
