package com.loopers.interfaces.api.product;

import com.loopers.application.product.out.ProductDetailInfo;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public class ProductV1Dto {
    
    public record ProductInfoResponse(
            Long id,
            String name,
            String description,
            String saleStatus,
            BigDecimal originalPrice,
            BigDecimal sellingPrice,
            Long likeCount,
            Long brandId,
            String brandName
            // 상품 id, 상품명, 상품설명, 판매상태, 원가, 할인가, 브랜드 id, 브랜드명, 좋아요 수
    ){
        public static ProductInfoResponse from(ProductDetailInfo info){
            return new ProductInfoResponse(
                    info.id(),
                    info.name(),
                    info.description(),
                    info.saleStatus(),
                    info.originalPrice(),
                    info.sellingPrice(),
                    info.likeCount(),
                    info.brandId(),
                    info.brandName()
            );
        }
    }

    public record ProductListResponse(
            List<ProductWithLikeCountDto> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious
    ) {
        public static ProductListResponse from(Page<ProductWithLikeCountDto> productPage) {
            return new ProductListResponse(
                    productPage.getContent(),
                    productPage.getNumber(),
                    productPage.getSize(),
                    productPage.getTotalElements(),
                    productPage.getTotalPages(),
                    productPage.isFirst(),
                    productPage.isLast(),
                    productPage.hasNext(),
                    productPage.hasPrevious()
            );
        }
    }
}
