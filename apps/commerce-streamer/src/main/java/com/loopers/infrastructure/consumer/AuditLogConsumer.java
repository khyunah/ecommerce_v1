package com.loopers.infrastructure.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.BaseEvent;
import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import com.loopers.domain.event.EventTopics;
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
 * 감사 로그 Consumer
 * 모든 이벤트를 event_log 테이블에 저장하여 감사 추적 가능하도록 함
 */
@Slf4j
@Component
public class AuditLogConsumer extends BaseConsumer {

    private static final String CONSUMER_NAME = "AuditLogConsumer";
    
    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogConsumer(IdempotencyService idempotencyService,
                           @Qualifier("eventObjectMapper") ObjectMapper objectMapper,
                           EventLogRepository eventLogRepository) {
        super(idempotencyService, objectMapper);
        this.eventLogRepository = eventLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 카탈로그 이벤트 처리 (catalog-events 토픽)
     */
    @KafkaListener(
        topics = EventTopics.CATALOG_EVENTS,
        groupId = "audit-log-consumer-group",
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
     * 주문 이벤트 처리 (order-events 토픽)
     */
    @KafkaListener(
        topics = EventTopics.ORDER_EVENTS,
        groupId = "audit-log-consumer-group",
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
        try {
            // 이벤트를 JSON으로 직렬화
            String payload = objectMapper.writeValueAsString(event);
            
            // EventLog 엔티티 생성
            EventLog eventLog = EventLog.create(
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAggregateType(),
                payload,
                event.getOccurredAt()
            );
            
            // 데이터베이스에 저장
            eventLogRepository.save(eventLog);
            
            log.debug("Audit log saved - EventId: {}, EventType: {}, AggregateId: {}", 
                    event.getEventId(), event.getEventType(), event.getAggregateId());
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for audit log - EventId: {}, EventType: {}, Error: {}", 
                    event.getEventId(), event.getEventType(), e.getMessage(), e);
            throw new RuntimeException("Failed to process audit log", e);
        }
    }

    @Override
    protected String getConsumerName() {
        return CONSUMER_NAME;
    }
}
