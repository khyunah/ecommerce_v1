package com.loopers.domain.event.order;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.loopers.domain.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 결제 완료 이벤트
 * - 판매량 집계 필요
 */
@JsonTypeName("OrderPaid")
@Getter
@NoArgsConstructor
public class OrderPaidEvent extends BaseEvent {

    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private List<OrderItem> orderItems;

    public OrderPaidEvent(Long orderId, Long userId, BigDecimal totalAmount, List<OrderItem> orderItems) {
        super("OrderPaid");
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
    }

    @Override
    public String getPartitionKey() {
        return orderId.toString();
    }

    @Override
    public String getAggregateId() {
        return orderId.toString();
    }

    @Override
    public String getAggregateType() {
        return "Order";
    }

    @Getter
    @NoArgsConstructor
    public static class OrderItem {
        private Long productId;
        private String productName;
        private Long quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public OrderItem(Long productId, String productName, Long quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
