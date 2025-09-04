package com.loopers.domain.metrics;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 일별 상품 메트릭스 집계 테이블
 * 좋아요 수, 판매량, 조회 수 등을 일별로 집계
 */
@Entity
@Getter
@Table(
    name = "product_metrics",
    indexes = {
        @Index(name = "idx_product_metrics_product_date", columnList = "product_id, metric_date"),
        @Index(name = "idx_product_metrics_metric_date", columnList = "metric_date"),
        @Index(name = "idx_product_metrics_product_id", columnList = "product_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_metrics_product_date", columnNames = {"product_id", "metric_date"})
    }
)
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetrics extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "likes_count", nullable = false)
    private Long likesCount = 0L;

    @Column(name = "sales_count", nullable = false)
    private Long salesCount = 0L;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "likes_delta", nullable = false)
    private Long likesDelta = 0L; // 하루 동안의 변화량

    @Column(name = "sales_delta", nullable = false)
    private Long salesDelta = 0L; // 하루 동안의 변화량

    @Column(name = "views_delta", nullable = false)
    private Long viewsDelta = 0L; // 하루 동안의 변화량

    public static ProductMetrics create(Long productId, LocalDate metricDate) {
        return new ProductMetrics(productId, metricDate, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    public void incrementLikes(long count) {
        this.likesCount += count;
        this.likesDelta += count;
    }

    public void decrementLikes(long count) {
        this.likesCount = Math.max(0, this.likesCount - count);
        this.likesDelta -= count;
    }

    public void incrementSales(long count) {
        this.salesCount += count;
        this.salesDelta += count;
    }

    public void incrementViews(long count) {
        this.viewCount += count;
        this.viewsDelta += count;
    }

    public void resetDelta() {
        this.likesDelta = 0L;
        this.salesDelta = 0L;
        this.viewsDelta = 0L;
    }
}
