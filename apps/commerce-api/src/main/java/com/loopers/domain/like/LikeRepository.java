package com.loopers.domain.like;

import com.loopers.domain.like.dto.LikedProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LikeRepository {
    Like save(Like like);
    boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
    int delete(Long refUserId, Long refProductId);
    Page<LikedProductDto> findLikedProductsByRefUserId(Long refUserId, Pageable pageable);
    long countLikeByProductId(Long productId);
    List<Like> findAllByProductId(Long productId1);
    List<Like> findAllByUserIdAndProductId(Long userId1, Long productId1);
    void deleteAll();
}
