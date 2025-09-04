package com.loopers.application.metrics;

import lombok.Builder;
import lombok.Getter;

/**
 * 상품 랭킹 정보
 */
@Getter
@Builder
public class ProductRanking {
    
    private final Long productId;
    private final int rank;
    private final long viewCount;
    private final long likesCount;
    private final long salesCount;
}
