package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderSummaryResult;
import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderV1Dto {
    public record OrderCreateRequest(
            List<OrderItemRequest> items,
            String orderSeq,
            Long couponId
    ){
        public static OrderCreateCommand toOrderCreateCommand(Long userId, OrderCreateRequest request){
            return new OrderCreateCommand(
                    userId,
                    OrderItemRequest.toOrderItemList(request.items),
                    request.orderSeq(),
                    request.couponId
            );
        }
    }
    public record OrderItemRequest(
            Long productId,
            int quantity
    ){
        public static List<OrderItemCriteria> toOrderItemList(List<OrderItemRequest> items){
            List<OrderItemCriteria> result = new ArrayList<>();
            for (OrderItemRequest item : items) {
                result.add(new OrderItemCriteria(item.productId, item.quantity));
            }
            return result;
        }
    }

    public record OrderCreateResponse(
            Long orderId,
            String status,
            LocalDateTime orderedAt,
            Long price
    ){
        public static OrderCreateResponse from(OrderSummaryResult result){
            return new OrderCreateResponse(
                    result.orderId(),
                    result.status(),
                    result.orderedAt(),
                    result.price()
            );
        }
    }
}
