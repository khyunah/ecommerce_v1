package com.loopers.application.like.out;

import com.loopers.domain.like.dto.LikedProductDto;

import java.math.BigDecimal;

public record LikedProductsResult(
        Long productId,
        String productName,
        BigDecimal originalPrice,
        BigDecimal sellingPrice,
        String saleStatus,
        Long likeId,
        Boolean liked
) {
    public static LikedProductsResult from(LikedProductDto likedProductDto) {
        return new LikedProductsResult(
                likedProductDto.productId(),
                likedProductDto.productName(),
                likedProductDto.originalPrice(),
                likedProductDto.sellingPrice(),
                likedProductDto.saleStatus(),
                likedProductDto.likeId(),
                likedProductDto.liked()
        );
    }
}
