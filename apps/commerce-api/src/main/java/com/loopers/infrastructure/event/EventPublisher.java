package com.loopers.infrastructure.event;

import com.loopers.domain.event.BaseEvent;

/**
 * 이벤트 발행 서비스 인터페이스
 */
public interface EventPublisher {

    /**
     * 카탈로그 이벤트 발행 (key=productId)
     */
    void publishCatalogEvent(BaseEvent event);

    /**
     * 주문 이벤트 발행 (key=orderId)
     */
    void publishOrderEvent(BaseEvent event);

    /**
     * 이벤트 발행 (토픽 지정)
     */
    void publishEvent(String topic, BaseEvent event);
}
