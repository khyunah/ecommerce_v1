package com.loopers.application.product.out;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;

import java.math.BigDecimal;

// 상품 id, 상품명, 상품설명, 판매상태, 원가, 할인가, 브랜드 id, 브랜드명, 좋아요 수
public record ProductDetailInfo(
        Long id,
        String name,
        String description,
        String saleStatus,
        BigDecimal originalPrice,
        BigDecimal sellingPrice,
        Long likeCount,
        Long brandId,
        String brandName
) {
    public static ProductDetailInfo from(Product product, Long likeCount,Brand brand) {
        return new ProductDetailInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSaleStatus().toString(),
                product.getOriginalPrice().getValue(),
                product.getSellingPrice().getValue(),
                likeCount,
                brand.getId(),
                brand.getName()
        );
    }

}
