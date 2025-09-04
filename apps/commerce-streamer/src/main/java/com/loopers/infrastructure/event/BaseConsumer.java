package com.loopers.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * 모든 Consumer의 공통 기능을 제공하는 추상 클래스
 * - 멱등성 처리
 * - Manual Ack 처리
 * - 공통 로깅
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseConsumer {

    private final IdempotencyService idempotencyService;
    
    @Qualifier("eventObjectMapper")
    private final ObjectMapper objectMapper;

    /**
     * 하위 클래스에서 구현해야 하는 실제 비즈니스 로직
     */
    protected abstract void processEvent(BaseEvent event);

    /**
     * Consumer 이름을 반환 (멱등성 처리용)
     */
    protected abstract String getConsumerName();

    /**
     * 이벤트 처리 공통 로직
     * - 멱등성 체크
     * - 비즈니스 로직 실행
     * - Manual Ack
     */
    protected void handleEvent(String eventPayload, 
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                              @Header(KafkaHeaders.OFFSET) long offset,
                              Acknowledgment acknowledgment) {
        
        BaseEvent event = null;
        try {
            // 1. JSON 역직렬화
            event = deserializeEvent(eventPayload);
            final BaseEvent EVENT = event;
            log.info("Received event - Consumer: {}, Topic: {}, Partition: {}, Offset: {}, EventId: {}, EventType: {}",
                    getConsumerName(), topic, partition, offset, event.getEventId(), event.getEventType());

            // 2. 멱등성 처리와 비즈니스 로직 실행
            boolean processed = idempotencyService.processWithIdempotency(
                event.getEventId(),
                event.getEventType(),
                getConsumerName(),
                () -> processEvent(EVENT)
            );

            if (processed) {
                log.info("Event processed successfully - Consumer: {}, EventId: {}, EventType: {}",
                        getConsumerName(), event.getEventId(), event.getEventType());
            } else {
                log.info("Event skipped (already processed) - Consumer: {}, EventId: {}, EventType: {}",
                        getConsumerName(), event.getEventId(), event.getEventType());
            }

            // 3. Manual Ack
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process event - Consumer: {}, Topic: {}, Partition: {}, Offset: {}, EventId: {}, Error: {}",
                    getConsumerName(), topic, partition, offset, 
                    event != null ? event.getEventId() : "unknown", 
                    e.getMessage(), e);
            
            // TODO: Dead Letter Queue 처리 또는 재시도 로직 추가 가능
            // 현재는 단순히 ack하여 메시지를 넘어감 (중복 처리 방지를 위해)
            acknowledgment.acknowledge();
        }
    }

    /**
     * JSON 문자열을 BaseEvent로 역직렬화
     */
    private BaseEvent deserializeEvent(String eventPayload) {
        try {
            return objectMapper.readValue(eventPayload, BaseEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize event payload: {}, Error: {}", eventPayload, e.getMessage());
            throw new IllegalArgumentException("Invalid event payload", e);
        }
    }
}
