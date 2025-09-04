package com.loopers.infrastructure.event;

import com.loopers.domain.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 기본 파티션 키 전략
 * - Catalog Events: productId 기반 (동일 상품의 이벤트 순서 보장)
 * - Order Events: orderId 기반 (동일 주문의 이벤트 순서 보장)
 */
@Slf4j
@Component
public class DefaultPartitionKeyStrategy implements PartitionKeyStrategy {

    @Override
    public String generatePartitionKey(BaseEvent event) {
        String partitionKey = event.getPartitionKey();
        
        if (!isValidPartitionKey(partitionKey)) {
            log.warn("Invalid partition key for event - EventId: {}, EventType: {}, PartitionKey: {}", 
                    event.getEventId(), event.getEventType(), partitionKey);
            // Fallback: 이벤트 타입 기반
            partitionKey = event.getEventType();
        }
        
        log.debug("Generated partition key - EventId: {}, EventType: {}, PartitionKey: {}", 
                event.getEventId(), event.getEventType(), partitionKey);
        
        return partitionKey;
    }

    @Override
    public boolean isValidPartitionKey(String partitionKey) {
        return StringUtils.hasText(partitionKey) 
                && partitionKey.length() <= 255  // Kafka 파티션 키 길이 제한
                && !partitionKey.contains("\n")  // 개행 문자 제외
                && !partitionKey.contains("\r"); // 캐리지 리턴 제외
    }

    /**
     * 파티션 키에 접두사를 추가하여 구분성을 높인다
     */
    public String addPrefix(String prefix, String key) {
        if (!StringUtils.hasText(prefix) || !StringUtils.hasText(key)) {
            return key;
        }
        return prefix + ":" + key;
    }

    /**
     * 상품 관련 파티션 키 생성
     */
    public String createProductPartitionKey(Long productId) {
        return addPrefix("product", productId.toString());
    }

    /**
     * 주문 관련 파티션 키 생성
     */
    public String createOrderPartitionKey(Long orderId) {
        return addPrefix("order", orderId.toString());
    }
}
