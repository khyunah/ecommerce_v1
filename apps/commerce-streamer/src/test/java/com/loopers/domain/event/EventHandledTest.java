package com.loopers.domain.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventHandledTest {

    @Test
    void create_정적팩토리메서드로_EventHandled_생성() {
        // given
        String eventId = "test-event-id";
        String eventType = "ProductCreated";
        String consumerName = "AuditLogConsumer";

        // when
        EventHandled eventHandled = EventHandled.create(eventId, eventType, consumerName);

        // then
        assertThat(eventHandled.getEventId()).isEqualTo(eventId);
        assertThat(eventHandled.getEventType()).isEqualTo(eventType);
        assertThat(eventHandled.getConsumerName()).isEqualTo(consumerName);
        assertThat(eventHandled.getProcessedAt()).isNotNull();
    }
}
