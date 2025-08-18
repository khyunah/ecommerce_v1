package com.loopers.interfaces.api.product;

import com.loopers.domain.product.vo.SaleStatus;

import java.math.BigDecimal;

public record ProductWithLikeCountDto(
        Long productId,
        String productName,
        BigDecimal originalPrice,
        BigDecimal sellingPrice,
        SaleStatus saleStatus,
        Long brandId,
        String brandName,
        Long likeCount
) {
}
