package com.loopers.interfaces.api.product;

import com.loopers.domain.product.vo.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductWithLikeCountDto {
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private SaleStatus saleStatus;
    private Long brandId;
    private String brandName;
    private Long likeCount;
}
