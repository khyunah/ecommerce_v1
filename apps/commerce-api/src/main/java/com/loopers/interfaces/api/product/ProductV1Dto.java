package com.loopers.interfaces.api.product;

import com.loopers.application.product.out.ProductDetailInfo;

public class ProductV1Dto {
    public record ProductInfoResponse(ProductDetailInfo productDetailInfo
            // 상품 id, 상품명, 상품설명, 판매상태, 원가, 할인가, 브랜드 id, 브랜드명, 좋아요 수
    ){
        public static ProductInfoResponse from(ProductDetailInfo productDetailInfo){
            return new ProductInfoResponse(productDetailInfo);
        }
    }
}
