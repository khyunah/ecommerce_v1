package com.loopers.domain.event;

/**
 * Kafka 토픽 상수 관리
 */
public final class EventTopics {
    
    // 카탈로그 관련 이벤트 토픽 (key=productId)
    public static final String CATALOG_EVENTS = "catalog-events";
    
    // 주문 관련 이벤트 토픽 (key=orderId)
    public static final String ORDER_EVENTS = "order-events";
    
    private EventTopics() {
        // 유틸리티 클래스
    }
}
