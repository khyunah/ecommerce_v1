package com.loopers.domain.metrics;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMetricsTest {

    @Test
    void create_정적팩토리메서드로_ProductMetrics_생성() {
        // given
        Long productId = 1L;
        LocalDate metricDate = LocalDate.of(2025, 9, 4);

        // when
        ProductMetrics metrics = ProductMetrics.create(productId, metricDate);

        // then
        assertThat(metrics.getProductId()).isEqualTo(productId);
        assertThat(metrics.getMetricDate()).isEqualTo(metricDate);
        assertThat(metrics.getLikesCount()).isZero();
        assertThat(metrics.getSalesCount()).isZero();
        assertThat(metrics.getViewCount()).isZero();
        assertThat(metrics.getLikesDelta()).isZero();
        assertThat(metrics.getSalesDelta()).isZero();
        assertThat(metrics.getViewsDelta()).isZero();
    }

    @Test
    void incrementLikes_좋아요_수_증가() {
        // given
        ProductMetrics metrics = ProductMetrics.create(1L, LocalDate.now());
        long incrementCount = 5L;

        // when
        metrics.incrementLikes(incrementCount);

        // then
        assertThat(metrics.getLikesCount()).isEqualTo(incrementCount);
        assertThat(metrics.getLikesDelta()).isEqualTo(incrementCount);
    }

    @Test
    void decrementLikes_좋아요_수_감소() {
        // given
        ProductMetrics metrics = ProductMetrics.create(1L, LocalDate.now());
        metrics.incrementLikes(10L);
        long decrementCount = 3L;

        // when
        metrics.decrementLikes(decrementCount);

        // then
        assertThat(metrics.getLikesCount()).isEqualTo(7L);
        assertThat(metrics.getLikesDelta()).isEqualTo(7L); // 10 + (-3)
    }

    @Test
    void decrementLikes_0보다_작아지지_않음() {
        // given
        ProductMetrics metrics = ProductMetrics.create(1L, LocalDate.now());
        long decrementCount = 5L;

        // when
        metrics.decrementLikes(decrementCount);

        // then
        assertThat(metrics.getLikesCount()).isZero();
        assertThat(metrics.getLikesDelta()).isEqualTo(-5L);
    }

    @Test
    void resetDelta_델타값_초기화() {
        // given
        ProductMetrics metrics = ProductMetrics.create(1L, LocalDate.now());
        metrics.incrementLikes(5L);
        metrics.incrementSales(3L);
        metrics.incrementViews(100L);

        // when
        metrics.resetDelta();

        // then
        assertThat(metrics.getLikesCount()).isEqualTo(5L); // 누적값은 유지
        assertThat(metrics.getSalesCount()).isEqualTo(3L);
        assertThat(metrics.getViewCount()).isEqualTo(100L);
        
        assertThat(metrics.getLikesDelta()).isZero(); // 델타는 초기화
        assertThat(metrics.getSalesDelta()).isZero();
        assertThat(metrics.getViewsDelta()).isZero();
    }
}
