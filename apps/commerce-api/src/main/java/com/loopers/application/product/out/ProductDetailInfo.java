package com.loopers.application.product.out;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;

import java.math.BigDecimal;

// 상품 id, 상품명, 상품설명, 판매상태, 원가, 할인가, 브랜드 id, 브랜드명,
// 좋아요 수, 좋아요 여부 --> 아직
public record ProductDetailInfo(
        Long id,
        String name,
        String description,
        String saleStatus,
        BigDecimal originalPrice,
        BigDecimal sellingPrice,
        Long brandId,
        String brandName
) {
    public static ProductDetailInfo from(Product product, Brand brand) {
        return new ProductDetailInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSaleStatus().name(),
                product.getOriginalPrice().getValue(),
                product.getSellingPrice().getValue(),
                brand.getId(),
                brand.getName()
        );
    }

}
