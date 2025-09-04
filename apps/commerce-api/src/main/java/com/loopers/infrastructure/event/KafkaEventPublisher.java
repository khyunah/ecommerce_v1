package com.loopers.infrastructure.event;

import com.loopers.domain.event.BaseEvent;
import com.loopers.domain.event.EventTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 기반 이벤트 발행 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCatalogEvent(BaseEvent event) {
        publishEvent(EventTopics.CATALOG_EVENTS, event);
    }

    @Override
    public void publishOrderEvent(BaseEvent event) {
        publishEvent(EventTopics.ORDER_EVENTS, event);
    }

    @Override
    public void publishEvent(String topic, BaseEvent event) {
        String partitionKey = event.getPartitionKey();
        
        log.info("Publishing event - Topic: {}, EventType: {}, EventId: {}, PartitionKey: {}", 
                topic, event.getEventType(), event.getEventId(), partitionKey);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, partitionKey, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event published successfully - EventId: {}, Offset: {}, Partition: {}", 
                        event.getEventId(), 
                        result.getRecordMetadata().offset(), 
                        result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish event - EventId: {}, Error: {}", 
                        event.getEventId(), ex.getMessage(), ex);
                // TODO: 실패 시 처리 로직 (Dead Letter Queue, 재시도 등)
                handlePublishFailure(event, ex);
            }
        });
    }

    /**
     * 이벤트 발행 실패 처리
     */
    private void handlePublishFailure(BaseEvent event, Throwable ex) {
        // TODO: Phase 3-3에서 구현
        // 1. Dead Letter Queue에 저장
        // 2. 알림 발송
        // 3. 메트릭 수집
        log.error("Event publish failed - storing for retry. EventId: {}, EventType: {}", 
                event.getEventId(), event.getEventType());
    }
}
