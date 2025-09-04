package com.loopers.application.metrics;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 메트릭스 성장률 분석 정보
 */
@Getter
@Builder
public class MetricsGrowthAnalysis {
    
    private final Long productId;
    private final LocalDate date;
    private final boolean hasYesterdayData;
    
    // 오늘 데이터
    private final long todayLikes;
    private final long todayViews;
    private final long todaySales;
    
    // 어제 데이터
    private final long yesterdayLikes;
    private final long yesterdayViews;
    private final long yesterdaySales;
    
    // 성장률 (%)
    private final double likesGrowthRate;
    private final double viewsGrowthRate;
    private final double salesGrowthRate;
    
    public static MetricsGrowthAnalysis noData(Long productId, LocalDate date) {
        return MetricsGrowthAnalysis.builder()
            .productId(productId)
            .date(date)
            .hasYesterdayData(false)
            .todayLikes(0)
            .todayViews(0)
            .todaySales(0)
            .yesterdayLikes(0)
            .yesterdayViews(0)
            .yesterdaySales(0)
            .likesGrowthRate(0.0)
            .viewsGrowthRate(0.0)
            .salesGrowthRate(0.0)
            .build();
    }
}
