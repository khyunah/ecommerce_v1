package com.loopers.domain.event.catalog;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.loopers.domain.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 변경 이벤트
 * - 캐시 삭제 필요
 */
@JsonTypeName("StockAdjusted")
@Getter
@NoArgsConstructor
public class StockAdjustedEvent extends BaseEvent {

    private Long productId;
    private Long previousStock;
    private Long currentStock;
    private Long adjustedQuantity; // 변경량 (음수일 수 있음)
    private String reason; // 변경 사유 (구매, 입고, 조정 등)

    public StockAdjustedEvent(Long productId, Long previousStock, Long currentStock, String reason) {
        super("StockAdjusted");
        this.productId = productId;
        this.previousStock = previousStock;
        this.currentStock = currentStock;
        this.adjustedQuantity = currentStock - previousStock;
        this.reason = reason;
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

    public boolean isStockDepleted() {
        return currentStock == 0L;
    }
}
