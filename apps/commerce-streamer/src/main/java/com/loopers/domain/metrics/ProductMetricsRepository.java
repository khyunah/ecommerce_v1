package com.loopers.domain.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 상품 메트릭스 관리를 위한 Repository
 */
public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, Long> {

    /**
     * 특정 상품의 특정 날짜 메트릭스 조회
     */
    Optional<ProductMetrics> findByProductIdAndMetricDate(Long productId, LocalDate metricDate);

    /**
     * 특정 상품의 기간별 메트릭스 조회
     */
    List<ProductMetrics> findByProductIdAndMetricDateBetweenOrderByMetricDateDesc(
        Long productId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 날짜의 모든 상품 메트릭스 조회
     */
    List<ProductMetrics> findByMetricDate(LocalDate metricDate);

    /**
     * 특정 상품의 전체 메트릭스 조회 (최신순)
     */
    List<ProductMetrics> findByProductIdOrderByMetricDateDesc(Long productId);

    /**
     * 좋아요 수 증가
     */
    @Modifying
    @Query("UPDATE ProductMetrics p SET p.likesCount = p.likesCount + :delta, p.likesDelta = p.likesDelta + :delta WHERE p.productId = :productId AND p.metricDate = :metricDate")
    int incrementLikesCount(@Param("productId") Long productId, 
                           @Param("metricDate") LocalDate metricDate, 
                           @Param("delta") Long delta);

    /**
     * 판매 수 증가
     */
    @Modifying
    @Query("UPDATE ProductMetrics p SET p.salesCount = p.salesCount + :delta, p.salesDelta = p.salesDelta + :delta WHERE p.productId = :productId AND p.metricDate = :metricDate")
    int incrementSalesCount(@Param("productId") Long productId, 
                           @Param("metricDate") LocalDate metricDate, 
                           @Param("delta") Long delta);

    /**
     * 조회 수 증가
     */
    @Modifying
    @Query("UPDATE ProductMetrics p SET p.viewCount = p.viewCount + :delta, p.viewsDelta = p.viewsDelta + :delta WHERE p.productId = :productId AND p.metricDate = :metricDate")
    int incrementViewCount(@Param("productId") Long productId, 
                          @Param("metricDate") LocalDate metricDate, 
                          @Param("delta") Long delta);

    /**
     * 일별 델타 값 초기화 (배치 작업용)
     */
    @Modifying
    @Query("UPDATE ProductMetrics p SET p.likesDelta = 0, p.salesDelta = 0, p.viewsDelta = 0 WHERE p.metricDate = :metricDate")
    int resetDeltaForDate(@Param("metricDate") LocalDate metricDate);
}
