package com.loopers.application.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 상품 메트릭스 조회 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;

    /**
     * 특정 상품의 특정 날짜 메트릭스 조회
     */
    public Optional<ProductMetrics> getProductMetrics(Long productId, LocalDate date) {
        return productMetricsRepository.findByProductIdAndMetricDate(productId, date);
    }

    /**
     * 특정 상품의 기간별 메트릭스 조회
     */
    public List<ProductMetrics> getProductMetricsByDateRange(Long productId, LocalDate startDate, LocalDate endDate) {
        return productMetricsRepository.findByProductIdAndMetricDateBetweenOrderByMetricDateDesc(
            productId, startDate, endDate);
    }

    /**
     * 특정 상품의 전체 메트릭스 조회 (최신순)
     */
    public List<ProductMetrics> getAllProductMetrics(Long productId) {
        return productMetricsRepository.findByProductIdOrderByMetricDateDesc(productId);
    }

    /**
     * 특정 날짜의 모든 상품 메트릭스 조회
     */
    public List<ProductMetrics> getDailyMetrics(LocalDate date) {
        return productMetricsRepository.findByMetricDate(date);
    }

    /**
     * 상품별 메트릭스 요약 정보
     */
    public ProductMetricsSummary getProductMetricsSummary(Long productId, LocalDate startDate, LocalDate endDate) {
        List<ProductMetrics> metrics = getProductMetricsByDateRange(productId, startDate, endDate);
        
        if (metrics.isEmpty()) {
            return ProductMetricsSummary.empty(productId, startDate, endDate);
        }

        long totalLikes = metrics.stream().mapToLong(ProductMetrics::getLikesCount).max().orElse(0);
        long totalSales = metrics.stream().mapToLong(ProductMetrics::getSalesCount).max().orElse(0);
        long totalViews = metrics.stream().mapToLong(ProductMetrics::getViewCount).max().orElse(0);
        
        long totalLikesDelta = metrics.stream().mapToLong(ProductMetrics::getLikesDelta).sum();
        long totalSalesDelta = metrics.stream().mapToLong(ProductMetrics::getSalesDelta).sum();
        long totalViewsDelta = metrics.stream().mapToLong(ProductMetrics::getViewsDelta).sum();
        
        return ProductMetricsSummary.builder()
            .productId(productId)
            .startDate(startDate)
            .endDate(endDate)
            .totalLikes(totalLikes)
            .totalSales(totalSales)
            .totalViews(totalViews)
            .periodLikesDelta(totalLikesDelta)
            .periodSalesDelta(totalSalesDelta)
            .periodViewsDelta(totalViewsDelta)
            .dayCount(metrics.size())
            .build();
    }

    /**
     * 인기 상품 랭킹 (특정 기간 기준)
     */
    public List<ProductRanking> getPopularProductsByViews(LocalDate startDate, LocalDate endDate, int limit) {
        List<ProductMetrics> allMetrics = productMetricsRepository.findByMetricDate(endDate);
        
        return allMetrics.stream()
            .sorted((m1, m2) -> Long.compare(m2.getViewCount(), m1.getViewCount()))
            .limit(limit)
            .map(metrics -> ProductRanking.builder()
                .productId(metrics.getProductId())
                .rank(0) // 순위는 별도로 설정
                .viewCount(metrics.getViewCount())
                .likesCount(metrics.getLikesCount())
                .salesCount(metrics.getSalesCount())
                .build())
            .toList();
    }

    /**
     * 메트릭스 성장률 분석 (전일 대비)
     */
    public MetricsGrowthAnalysis getGrowthAnalysis(Long productId, LocalDate date) {
        Optional<ProductMetrics> todayMetrics = getProductMetrics(productId, date);
        Optional<ProductMetrics> yesterdayMetrics = getProductMetrics(productId, date.minusDays(1));
        
        if (todayMetrics.isEmpty()) {
            return MetricsGrowthAnalysis.noData(productId, date);
        }
        
        ProductMetrics today = todayMetrics.get();
        
        if (yesterdayMetrics.isEmpty()) {
            return MetricsGrowthAnalysis.builder()
                .productId(productId)
                .date(date)
                .hasYesterdayData(false)
                .todayLikes(today.getLikesCount())
                .todayViews(today.getViewCount())
                .todaySales(today.getSalesCount())
                .build();
        }
        
        ProductMetrics yesterday = yesterdayMetrics.get();
        
        return MetricsGrowthAnalysis.builder()
            .productId(productId)
            .date(date)
            .hasYesterdayData(true)
            .todayLikes(today.getLikesCount())
            .todayViews(today.getViewCount())
            .todaySales(today.getSalesCount())
            .yesterdayLikes(yesterday.getLikesCount())
            .yesterdayViews(yesterday.getViewCount())
            .yesterdaySales(yesterday.getSalesCount())
            .likesGrowthRate(calculateGrowthRate(yesterday.getLikesCount(), today.getLikesCount()))
            .viewsGrowthRate(calculateGrowthRate(yesterday.getViewCount(), today.getViewCount()))
            .salesGrowthRate(calculateGrowthRate(yesterday.getSalesCount(), today.getSalesCount()))
            .build();
    }
    
    private double calculateGrowthRate(long previousValue, long currentValue) {
        if (previousValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }
        return ((double) (currentValue - previousValue) / previousValue) * 100.0;
    }
}
