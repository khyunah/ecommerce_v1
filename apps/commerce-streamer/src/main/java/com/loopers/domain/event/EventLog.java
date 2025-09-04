package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 감사 로그를 위한 이벤트 저장 테이블
 * 모든 이벤트를 저장하여 추적 가능하도록 함
 */
@Entity
@Getter
@Table(
    name = "event_log",
    indexes = {
        @Index(name = "idx_event_log_event_id", columnList = "event_id"),
        @Index(name = "idx_event_log_event_type", columnList = "event_type"),
        @Index(name = "idx_event_log_created_at", columnList = "created_at"),
        @Index(name = "idx_event_log_aggregate_id", columnList = "aggregate_id")
    }
)
@NoArgsConstructor
@AllArgsConstructor
public class EventLog extends BaseEntity {

    @Column(name = "event_id", nullable = false, length = 255)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId; // productId, orderId 등

    @Column(name = "aggregate_type", length = 50)
    private String aggregateType; // Product, Order 등

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload; // JSON 형태의 이벤트 데이터

    @Column(name = "event_occurred_at", nullable = false)
    private LocalDateTime eventOccurredAt;

    public static EventLog create(String eventId, String eventType, String aggregateId, 
                                String aggregateType, String payload, LocalDateTime eventOccurredAt) {
        return new EventLog(eventId, eventType, aggregateId, aggregateType, payload, eventOccurredAt);
    }
}
