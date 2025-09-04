package com.loopers.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.BaseEvent;
import com.loopers.domain.event.EventTopics;
import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import com.loopers.infrastructure.cache.ProductCacheEvictService;
import com.loopers.infrastructure.event.BaseConsumer;
import com.loopers.infrastructure.event.IdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 캐시 무효화 Consumer
 * StockAdjusted, LikeChanged 이벤트 발생 시 상황에 따라 Redis 캐시 삭제
 */
@Slf4j
@Component
public class CacheEvictConsumer extends BaseConsumer {

    private static final String CONSUMER_NAME = "CacheEvictConsumer";
    
    private final ProductCacheEvictService cacheEvictService;

    public CacheEvictConsumer(IdempotencyService idempotencyService,
                             @Qualifier("eventObjectMapper") ObjectMapper objectMapper,
                             ProductCacheEvictService cacheEvictService) {
        super(idempotencyService, objectMapper);
        this.cacheEvictService = cacheEvictService;
    }

    /**
     * 카탈로그 이벤트 처리 - 재고/좋아요 변경 시 캐시 삭제
     */
    @KafkaListener(
        topics = EventTopics.CATALOG_EVENTS,
        groupId = "cache-evict-consumer-group",
        containerFactory = "defaultBatchListenerContainerFactory"
    )
    public void handleCatalogEvents(@Payload String eventPayload,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {
        
        handleEvent(eventPayload, topic, partition, offset, acknowledgment);
    }

    @Override
    protected void processEvent(BaseEvent event) {
        switch (event.getEventType()) {
            case "StockAdjusted" -> processStockAdjustedEvent((StockAdjustedEvent) event);
            case "LikeChanged" -> processLikeChangedEvent((LikeChangedEvent) event);
            default -> log.debug("Event type not handled by CacheEvictConsumer: {}", event.getEventType());
        }
    }

    /**
     * 재고 변경 이벤트 처리
     * - 재고 소진 시: 상품 상세, 목록 캐시 삭제
     * - 일반 재고 변경 시: 상품 상세 캐시만 삭제
     */
    private void processStockAdjustedEvent(StockAdjustedEvent event) {
        Long productId = event.getProductId();
        
        // 상품 상세 캐시는 항상 삭제
        cacheEvictService.evictProductDetailCache(productId);
        
        // 재고 소진 시 추가 캐시 삭제
        if (event.isStockDepleted()) {
            cacheEvictService.evictProductListCaches(productId);
            cacheEvictService.evictPopularProductsCaches();
            
            log.info("Product stock depleted - comprehensive cache eviction - ProductId: {}", productId);
        } else {
            log.debug("Product stock adjusted - detail cache evicted - ProductId: {}, NewStock: {}", 
                    productId, event.getCurrentStock());
        }
    }

    /**
     * 좋아요 변경 이벤트 처리
     * - 좋아요 수 변경 시: 상품 상세, 인기 상품 캐시 삭제
     */
    private void processLikeChangedEvent(LikeChangedEvent event) {
        Long productId = event.getProductId();
        
        // 상품 상세 캐시 삭제
        cacheEvictService.evictProductDetailCache(productId);
        
        // 인기 상품 관련 캐시 삭제 (좋아요 수가 랭킹에 영향)
        cacheEvictService.evictPopularProductsCaches();
        
        log.debug("Product like changed - cache evicted - ProductId: {}, NewLikes: {}, Action: {}", 
                productId, event.getCurrentLikeCount(), event.getAction());
    }

    @Override
    protected String getConsumerName() {
        return CONSUMER_NAME;
    }
}
