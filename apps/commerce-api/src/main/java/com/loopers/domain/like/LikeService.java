package com.loopers.domain.like;

import com.loopers.domain.like.dto.LikedProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeService {
    private final LikeRepository likeRepository;

    public boolean create(Like like) {
        // 상품당 1개의 좋아요만 가능 -> 멱등성 보장
        boolean isExisting = likeRepository.existsByRefUserIdAndRefProductId(like.getRefUserId(), like.getRefProductId());
        if(isExisting) return true;
        Like savedLike = likeRepository.save(like);

        return savedLike.getRefUserId().equals(like.getRefUserId());
    }

    public boolean delete(Like like) {
        boolean isExisting = likeRepository.existsByRefUserIdAndRefProductId(like.getRefUserId(), like.getRefProductId());
        if(!isExisting) return true;
        return likeRepository.delete(like.getRefUserId(), like.getRefProductId());
    }

    public Page<LikedProductDto> getLikedProducts(Long refUserId, Pageable pageable) {
        return likeRepository.findLikedProductsByRefUserId(refUserId, pageable);
    }

    public long countLikeByProductId(Long productId) {
        return likeRepository.countLikeByProductId(productId);

    }
}
