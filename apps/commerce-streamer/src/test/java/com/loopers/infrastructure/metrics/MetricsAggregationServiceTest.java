package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsAggregationServiceTest {

    @Mock
    private ProductMetricsRepository productMetricsRepository;

    @InjectMocks
    private MetricsAggregationService metricsAggregationService;

    @Test
    void 좋아요_메트릭스_업데이트_기존_데이터_있음() {
        // given
        Long productId = 1L;
        Long delta = 2L;
        LocalDate today = LocalDate.now();
        
        ProductMetrics existingMetrics = ProductMetrics.create(productId, today);
        existingMetrics.incrementLikes(5L); // 기존 좋아요 5개
        
        when(productMetricsRepository.findByProductIdAndMetricDate(productId, today))
            .thenReturn(Optional.of(existingMetrics));

        // when
        metricsAggregationService.updateLikesMetrics(productId, delta);

        // then
        ArgumentCaptor<ProductMetrics> captor = ArgumentCaptor.forClass(ProductMetrics.class);
        verify(productMetricsRepository).save(captor.capture());
        
        ProductMetrics savedMetrics = captor.getValue();
        assertThat(savedMetrics.getLikesCount()).isEqualTo(7L); // 5 + 2
        assertThat(savedMetrics.getLikesDelta()).isEqualTo(7L); // 5 + 2
    }

    @Test
    void 좋아요_메트릭스_업데이트_새_데이터() {
        // given
        Long productId = 1L;
        Long delta = 3L;
        LocalDate today = LocalDate.now();
        
        when(productMetricsRepository.findByProductIdAndMetricDate(productId, today))
            .thenReturn(Optional.empty());

        // when
        metricsAggregationService.updateLikesMetrics(productId, delta);

        // then
        ArgumentCaptor<ProductMetrics> captor = ArgumentCaptor.forClass(ProductMetrics.class);
        verify(productMetricsRepository).save(captor.capture());
        
        ProductMetrics savedMetrics = captor.getValue();
        assertThat(savedMetrics.getProductId()).isEqualTo(productId);
        assertThat(savedMetrics.getMetricDate()).isEqualTo(today);
        assertThat(savedMetrics.getLikesCount()).isEqualTo(3L);
        assertThat(savedMetrics.getLikesDelta()).isEqualTo(3L);
    }

    @Test
    void 좋아요_감소_처리() {
        // given
        Long productId = 1L;
        Long negativeDelta = -2L;
        LocalDate today = LocalDate.now();
        
        ProductMetrics existingMetrics = ProductMetrics.create(productId, today);
        existingMetrics.incrementLikes(5L); // 기존 좋아요 5개
        
        when(productMetricsRepository.findByProductIdAndMetricDate(productId, today))
            .thenReturn(Optional.of(existingMetrics));

        // when
        metricsAggregationService.updateLikesMetrics(productId, negativeDelta);

        // then
        ArgumentCaptor<ProductMetrics> captor = ArgumentCaptor.forClass(ProductMetrics.class);
        verify(productMetricsRepository).save(captor.capture());
        
        ProductMetrics savedMetrics = captor.getValue();
        assertThat(savedMetrics.getLikesCount()).isEqualTo(3L); // 5 - 2
    }

    @Test
    void 조회수_메트릭스_업데이트() {
        // given
        Long productId = 1L;
        Long count = 1L;
        LocalDate today = LocalDate.now();
        
        when(productMetricsRepository.findByProductIdAndMetricDate(productId, today))
            .thenReturn(Optional.empty());

        // when
        metricsAggregationService.updateViewMetrics(productId, count);

        // then
        ArgumentCaptor<ProductMetrics> captor = ArgumentCaptor.forClass(ProductMetrics.class);
        verify(productMetricsRepository).save(captor.capture());
        
        ProductMetrics savedMetrics = captor.getValue();
        assertThat(savedMetrics.getViewCount()).isEqualTo(1L);
        assertThat(savedMetrics.getViewsDelta()).isEqualTo(1L);
    }

    @Test
    void 판매량_메트릭스_업데이트() {
        // given
        Long productId = 1L;
        Long quantity = 3L;
        LocalDate today = LocalDate.now();
        
        when(productMetricsRepository.findByProductIdAndMetricDate(productId, today))
            .thenReturn(Optional.empty());

        // when
        metricsAggregationService.updateSalesMetrics(productId, quantity);

        // then
        ArgumentCaptor<ProductMetrics> captor = ArgumentCaptor.forClass(ProductMetrics.class);
        verify(productMetricsRepository).save(captor.capture());
        
        ProductMetrics savedMetrics = captor.getValue();
        assertThat(savedMetrics.getSalesCount()).isEqualTo(3L);
        assertThat(savedMetrics.getSalesDelta()).isEqualTo(3L);
    }

    @Test
    void 일별_델타_초기화() {
        // given
        LocalDate date = LocalDate.now();
        when(productMetricsRepository.resetDeltaForDate(date)).thenReturn(5);

        // when
        metricsAggregationService.resetDailyDelta(date);

        // then
        verify(productMetricsRepository).resetDeltaForDate(date);
    }
}
