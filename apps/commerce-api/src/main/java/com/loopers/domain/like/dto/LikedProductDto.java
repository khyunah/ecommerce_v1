package com.loopers.domain.like.dto;

import java.math.BigDecimal;

public record LikedProductDto(
        Long likeId,
        Long productId,
        String productName,
        BigDecimal originalPrice,
        BigDecimal sellingPrice,
        String saleStatus,
        boolean liked,
        Long totalLikeCount
) {
}
