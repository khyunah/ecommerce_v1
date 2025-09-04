package com.loopers.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import com.loopers.infrastructure.event.IdempotencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogConsumerTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventLogRepository eventLogRepository;

    @InjectMocks
    private AuditLogConsumer auditLogConsumer;

    @Test
    void 이벤트_감사로그_저장_성공() throws Exception {
        // given
        StockAdjustedEvent event = new StockAdjustedEvent(1L, 10L, 8L, "구매");
        String eventPayload = "{\"eventId\":\"" + event.getEventId() + "\",\"eventType\":\"StockAdjusted\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(eventPayload);

        // when
        auditLogConsumer.processEvent(event);

        // then
        ArgumentCaptor<EventLog> eventLogCaptor = ArgumentCaptor.forClass(EventLog.class);
        verify(eventLogRepository).save(eventLogCaptor.capture());

        EventLog savedEventLog = eventLogCaptor.getValue();
        assertThat(savedEventLog.getEventId()).isEqualTo(event.getEventId());
        assertThat(savedEventLog.getEventType()).isEqualTo(event.getEventType());
        assertThat(savedEventLog.getAggregateId()).isEqualTo(event.getAggregateId());
        assertThat(savedEventLog.getAggregateType()).isEqualTo(event.getAggregateType());
        assertThat(savedEventLog.getPayload()).isEqualTo(eventPayload);
        assertThat(savedEventLog.getEventOccurredAt()).isEqualTo(event.getOccurredAt());
    }

    @Test
    void Consumer_이름_반환() {
        // when
        String consumerName = auditLogConsumer.getConsumerName();

        // then
        assertThat(consumerName).isEqualTo("AuditLogConsumer");
    }

    @Test
    void JSON_직렬화_실패_시_예외_발생() throws Exception {
        // given
        StockAdjustedEvent event = new StockAdjustedEvent(1L, 10L, 8L, "구매");
        
        when(objectMapper.writeValueAsString(event))
            .thenThrow(new RuntimeException("Failed to process audit log"));

        // when & then
        try {
            auditLogConsumer.processEvent(event);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Failed to process audit log");
        }

        verify(eventLogRepository, never()).save(any(EventLog.class));
    }
}
