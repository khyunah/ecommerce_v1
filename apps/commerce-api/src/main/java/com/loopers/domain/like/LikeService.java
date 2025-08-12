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
        if(isExisting){
            throw new IllegalArgumentException("좋아요는 하나의 상품에 하나만 가능합니다.");
        }
        Like savedLike = likeRepository.save(like);

        return savedLike.getRefUserId().equals(like.getRefUserId());
    }

    public boolean delete(Like like) {
        boolean isExisting = likeRepository.existsByRefUserIdAndRefProductId(like.getRefUserId(), like.getRefProductId());
        if(!isExisting){
            throw new IllegalArgumentException("좋아요를 이미 삭제했습니다.");
        }
        int count = likeRepository.delete(like.getRefUserId(), like.getRefProductId());
        return count > -1;
    }

    public Page<LikedProductDto> getLikedProducts(Long refUserId, Pageable pageable) {
        return likeRepository.findLikedProductsByRefUserId(refUserId, pageable);
    }

    public long countLikeByProductId(Long productId) {
        return likeRepository.countLikeByProductId(productId);

    }
}
