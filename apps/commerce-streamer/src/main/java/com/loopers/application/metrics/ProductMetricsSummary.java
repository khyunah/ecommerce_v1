package com.loopers.application.metrics;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 상품 메트릭스 요약 정보
 */
@Getter
@Builder
public class ProductMetricsSummary {
    
    private final Long productId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    // 누적 값 (최종 값)
    private final long totalLikes;
    private final long totalSales;
    private final long totalViews;
    
    // 기간 내 변화량
    private final long periodLikesDelta;
    private final long periodSalesDelta;
    private final long periodViewsDelta;
    
    private final int dayCount;
    
    public static ProductMetricsSummary empty(Long productId, LocalDate startDate, LocalDate endDate) {
        return ProductMetricsSummary.builder()
            .productId(productId)
            .startDate(startDate)
            .endDate(endDate)
            .totalLikes(0)
            .totalSales(0)
            .totalViews(0)
            .periodLikesDelta(0)
            .periodSalesDelta(0)
            .periodViewsDelta(0)
            .dayCount(0)
            .build();
    }
}
