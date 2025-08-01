package com.loopers.domain.like;

import com.loopers.domain.like.dto.LikedProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikeRepository {
    Like save(Like like);
    boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
    boolean delete(Long refUserId, Long refProductId);
    Page<LikedProductDto> findLikedProductsByRefUserId(Long refUserId, Pageable pageable);
    long countLikeByProductId(Long productId);
}
