package com.loopers.infrastructure.event;

import com.loopers.domain.event.BaseEvent;

/**
 * 파티션 키 전략 인터페이스
 * 이벤트 순서 보장을 위한 파티션 키 결정 로직
 */
public interface PartitionKeyStrategy {

    /**
     * 이벤트에 대한 파티션 키를 생성한다
     * 동일한 파티션 키를 가진 이벤트들은 같은 파티션에 저장되어 순서가 보장된다
     */
    String generatePartitionKey(BaseEvent event);

    /**
     * 파티션 키가 유효한지 검증한다
     */
    boolean isValidPartitionKey(String partitionKey);
}
