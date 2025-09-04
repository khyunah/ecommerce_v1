package com.loopers.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.ProductViewedEvent;
import com.loopers.domain.event.order.OrderPaidEvent;
import com.loopers.infrastructure.event.IdempotencyService;
import com.loopers.infrastructure.metrics.MetricsAggregationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MetricsConsumerTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MetricsAggregationService metricsAggregationService;

    @InjectMocks
    private MetricsConsumer metricsConsumer;

    @Test
    void 좋아요_변경_이벤트_처리() {
        // given
        LikeChangedEvent event = new LikeChangedEvent(1L, 100L, 5L, 6L, "LIKED");

        // when
        metricsConsumer.processEvent(event);

        // then
        verify(metricsAggregationService).updateLikesMetrics(1L, 1L);
    }

    @Test
    void 상품_조회_이벤트_처리() {
        // given
        ProductViewedEvent event = new ProductViewedEvent(1L, 100L, "session-123");

        // when
        metricsConsumer.processEvent(event);

        // then
        verify(metricsAggregationService).updateViewMetrics(1L, 1L);
    }

    @Test
    void 주문_결제_완료_이벤트_처리() {
        // given
        List<OrderPaidEvent.OrderItem> orderItems = List.of(
            new OrderPaidEvent.OrderItem(1L, "상품1", 2L, BigDecimal.valueOf(25000)),
            new OrderPaidEvent.OrderItem(2L, "상품2", 1L, BigDecimal.valueOf(30000))
        );
        OrderPaidEvent event = new OrderPaidEvent(100L, 200L, BigDecimal.valueOf(80000), orderItems);

        // when
        metricsConsumer.processEvent(event);

        // then
        verify(metricsAggregationService).updateSalesMetrics(1L, 2L);
        verify(metricsAggregationService).updateSalesMetrics(2L, 1L);
    }

    @Test
    void Consumer_이름_반환() {
        // when
        String consumerName = metricsConsumer.getConsumerName();

        // then
        assertThat(consumerName).isEqualTo("MetricsConsumer");
    }
}
