package com.loopers.domain.event.catalog;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.loopers.domain.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좋아요 변경 이벤트
 * - 캐시 삭제 필요
 * - 메트릭스 집계 필요
 */
@JsonTypeName("LikeChanged")
@Getter
@NoArgsConstructor
public class LikeChangedEvent extends BaseEvent {

    private Long productId;
    private Long userId;
    private Long previousLikeCount;
    private Long currentLikeCount;
    private String action; // "LIKED" or "UNLIKED"

    public LikeChangedEvent(Long productId, Long userId, Long previousLikeCount, Long currentLikeCount, String action) {
        super("LikeChanged");
        this.productId = productId;
        this.userId = userId;
        this.previousLikeCount = previousLikeCount;
        this.currentLikeCount = currentLikeCount;
        this.action = action;
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

    public Long getLikeDelta() {
        return currentLikeCount - previousLikeCount;
    }

    public boolean isLiked() {
        return "LIKED".equals(action);
    }
}
