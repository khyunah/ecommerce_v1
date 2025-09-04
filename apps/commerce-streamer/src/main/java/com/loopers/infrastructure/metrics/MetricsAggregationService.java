package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 메트릭스 집계 서비스
 * product_metrics 테이블에 일별 메트릭스를 upsert
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsAggregationService {

    private final ProductMetricsRepository productMetricsRepository;

    /**
     * 좋아요 메트릭스 업데이트
     */
    @Transactional
    public void updateLikesMetrics(Long productId, Long delta) {
        LocalDate today = LocalDate.now();
        
        ProductMetrics metrics = findOrCreateProductMetrics(productId, today);
        
        if (delta > 0) {
            metrics.incrementLikes(delta);
        } else if (delta < 0) {
            metrics.decrementLikes(-delta); // 음수를 양수로 변환
        }
        
        productMetricsRepository.save(metrics);
        
        log.debug("Updated likes metrics - ProductId: {}, Date: {}, Delta: {}, NewCount: {}", 
                productId, today, delta, metrics.getLikesCount());
    }

    /**
     * 조회수 메트릭스 업데이트
     */
    @Transactional
    public void updateViewMetrics(Long productId, Long count) {
        LocalDate today = LocalDate.now();
        
        ProductMetrics metrics = findOrCreateProductMetrics(productId, today);
        metrics.incrementViews(count);
        
        productMetricsRepository.save(metrics);
        
        log.debug("Updated view metrics - ProductId: {}, Date: {}, Count: {}, NewTotal: {}", 
                productId, today, count, metrics.getViewCount());
    }

    /**
     * 판매량 메트릭스 업데이트
     */
    @Transactional
    public void updateSalesMetrics(Long productId, Long quantity) {
        LocalDate today = LocalDate.now();
        
        ProductMetrics metrics = findOrCreateProductMetrics(productId, today);
        metrics.incrementSales(quantity);
        
        productMetricsRepository.save(metrics);
        
        log.debug("Updated sales metrics - ProductId: {}, Date: {}, Quantity: {}, NewTotal: {}", 
                productId, today, quantity, metrics.getSalesCount());
    }

    /**
     * 상품별 일별 메트릭스 조회 또는 생성
     */
    private ProductMetrics findOrCreateProductMetrics(Long productId, LocalDate date) {
        return productMetricsRepository.findByProductIdAndMetricDate(productId, date)
                .orElseGet(() -> {
                    ProductMetrics newMetrics = ProductMetrics.create(productId, date);
                    log.debug("Created new ProductMetrics - ProductId: {}, Date: {}", productId, date);
                    return newMetrics;
                });
    }

    /**
     * 일별 델타 값 초기화 (배치 작업용)
     */
    @Transactional
    public void resetDailyDelta(LocalDate date) {
        int resetCount = productMetricsRepository.resetDeltaForDate(date);
        log.info("Reset daily delta for date: {}, affected rows: {}", date, resetCount);
    }

    /**
     * 메트릭스 데이터 검증 및 복구
     */
    @Transactional(readOnly = true)
    public void validateMetrics(Long productId, LocalDate date) {
        productMetricsRepository.findByProductIdAndMetricDate(productId, date)
                .ifPresent(metrics -> {
                    // 음수 값 검증
                    if (metrics.getLikesCount() < 0 || metrics.getSalesCount() < 0 || metrics.getViewCount() < 0) {
                        log.warn("Invalid negative metrics found - ProductId: {}, Date: {}, Metrics: [{}, {}, {}]", 
                                productId, date, metrics.getLikesCount(), metrics.getSalesCount(), metrics.getViewCount());
                    }
                    
                    // 이상한 델타 값 검증 (너무 큰 값)
                    long maxReasonableDelta = 10000L;
                    if (Math.abs(metrics.getLikesDelta()) > maxReasonableDelta ||
                        Math.abs(metrics.getSalesDelta()) > maxReasonableDelta ||
                        Math.abs(metrics.getViewsDelta()) > maxReasonableDelta) {
                        
                        log.warn("Suspicious large delta values found - ProductId: {}, Date: {}, Deltas: [{}, {}, {}]", 
                                productId, date, metrics.getLikesDelta(), metrics.getSalesDelta(), metrics.getViewsDelta());
                    }
                });
    }
}
