package com.loopers.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 모든 도메인 이벤트의 기본 클래스
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    protected String eventId;
    protected String eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    protected LocalDateTime occurredAt;
    
    protected Long version;

    protected BaseEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
        this.version = 1L;
    }

    /**
     * 파티션 키를 반환한다 (순서 보장을 위해)
     */
    public abstract String getPartitionKey();

    /**
     * 집계 루트 ID를 반환한다 (productId, orderId 등)
     */
    public abstract String getAggregateId();

    /**
     * 집계 루트 타입을 반환한다 (Product, Order 등)
     */
    public abstract String getAggregateType();
}
