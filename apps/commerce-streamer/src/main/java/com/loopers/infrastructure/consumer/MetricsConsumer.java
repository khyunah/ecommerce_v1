package com.loopers.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.BaseEvent;
import com.loopers.domain.event.EventTopics;
import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.ProductViewedEvent;
import com.loopers.domain.event.order.OrderPaidEvent;
import com.loopers.infrastructure.event.BaseConsumer;
import com.loopers.infrastructure.event.IdempotencyService;
import com.loopers.infrastructure.metrics.MetricsAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 메트릭스 집계 Consumer
 * 일별로 좋아요 수 / 판매량 / 조회 수를 product_metrics 테이블에 upsert
 */
@Slf4j
@Component
public class MetricsConsumer extends BaseConsumer {

    private static final String CONSUMER_NAME = "MetricsConsumer";
    
    private final MetricsAggregationService metricsAggregationService;

    public MetricsConsumer(IdempotencyService idempotencyService,
                          @Qualifier("eventObjectMapper") ObjectMapper objectMapper,
                          MetricsAggregationService metricsAggregationService) {
        super(idempotencyService, objectMapper);
        this.metricsAggregationService = metricsAggregationService;
    }

    /**
     * 카탈로그 이벤트 처리 - 좋아요, 조회수 집계
     */
    @KafkaListener(
        topics = EventTopics.CATALOG_EVENTS,
        groupId = "metrics-consumer-group",
        containerFactory = "defaultBatchListenerContainerFactory"
    )
    public void handleCatalogEvents(@Payload String eventPayload,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {
        
        handleEvent(eventPayload, topic, partition, offset, acknowledgment);
    }

    /**
     * 주문 이벤트 처리 - 판매량 집계
     */
    @KafkaListener(
        topics = EventTopics.ORDER_EVENTS,
        groupId = "metrics-consumer-group",
        containerFactory = "defaultBatchListenerContainerFactory"
    )
    public void handleOrderEvents(@Payload String eventPayload,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                @Header(KafkaHeaders.OFFSET) long offset,
                                Acknowledgment acknowledgment) {
        
        handleEvent(eventPayload, topic, partition, offset, acknowledgment);
    }

    @Override
    protected void processEvent(BaseEvent event) {
        switch (event.getEventType()) {
            case "LikeChanged" -> processLikeChangedEvent((LikeChangedEvent) event);
            case "ProductViewed" -> processProductViewedEvent((ProductViewedEvent) event);
            case "OrderPaid" -> processOrderPaidEvent((OrderPaidEvent) event);
            default -> log.debug("Event type not handled by MetricsConsumer: {}", event.getEventType());
        }
    }

    /**
     * 좋아요 변경 이벤트 처리
     */
    private void processLikeChangedEvent(LikeChangedEvent event) {
        Long productId = event.getProductId();
        Long likeDelta = event.getLikeDelta();
        
        metricsAggregationService.updateLikesMetrics(productId, likeDelta);
        
        log.debug("Processed LikeChanged event - ProductId: {}, Delta: {}", productId, likeDelta);
    }

    /**
     * 상품 조회 이벤트 처리
     */
    private void processProductViewedEvent(ProductViewedEvent event) {
        Long productId = event.getProductId();
        
        metricsAggregationService.updateViewMetrics(productId, 1L);
        
        log.debug("Processed ProductViewed event - ProductId: {}", productId);
    }

    /**
     * 주문 결제 완료 이벤트 처리
     */
    private void processOrderPaidEvent(OrderPaidEvent event) {
        // 주문 아이템별로 판매량 집계
        event.getOrderItems().forEach(orderItem -> {
            Long productId = orderItem.getProductId();
            Long quantity = orderItem.getQuantity();
            
            metricsAggregationService.updateSalesMetrics(productId, quantity);
            
            log.debug("Processed OrderPaid event - ProductId: {}, Quantity: {}", productId, quantity);
        });
    }

    @Override
    protected String getConsumerName() {
        return CONSUMER_NAME;
    }
}
