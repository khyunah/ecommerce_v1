package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이벤트 멱등성 처리를 위한 테이블
 * 처리된 이벤트를 추적하여 중복 처리를 방지
 */
@Entity
@Getter
@Table(
    name = "event_handled",
    indexes = {
        @Index(name = "idx_event_handled_event_id", columnList = "event_id", unique = true),
        @Index(name = "idx_event_handled_processed_at", columnList = "processed_at")
    }
)
@NoArgsConstructor
@AllArgsConstructor
public class EventHandled extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true, length = 255)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "consumer_name", nullable = false, length = 100)
    private String consumerName;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public static EventHandled create(String eventId, String eventType, String consumerName) {
        return new EventHandled(eventId, eventType, consumerName, LocalDateTime.now());
    }
}
