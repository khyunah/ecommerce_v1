package com.loopers.domain.event.catalog;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.loopers.domain.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 조회 이벤트
 * - 메트릭스 집계 필요
 */
@JsonTypeName("ProductViewed")
@Getter
@NoArgsConstructor
public class ProductViewedEvent extends BaseEvent {

    private Long productId;
    private Long userId;
    private String sessionId;

    public ProductViewedEvent(Long productId, Long userId, String sessionId) {
        super("ProductViewed");
        this.productId = productId;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    @Override
    public String getPartitionKey() {
        return productId.toString();
    }

    @Override
    public String getAggregateId() {
        return productId.toString();
    }

    @Override
    public String getAggregateType() {
        return "Product";
    }
}
