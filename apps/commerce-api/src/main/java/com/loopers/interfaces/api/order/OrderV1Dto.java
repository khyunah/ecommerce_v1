package com.loopers.interfaces.api.order;

import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.application.order.out.OrderCreateResult;
import com.loopers.application.order.out.OrderDetailResult;
import com.loopers.application.order.out.OrderResult;
import com.loopers.domain.order.Order;

import java.math.BigDecimal;
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
        public static OrderCreateResponse from(OrderCreateResult result){
            return new OrderCreateResponse(
                    result.orderId(),
                    result.status(),
                    result.orderedAt(),
                    result.price()
            );
        }
    }

    public record OrderResponse(
            Long orderId,
            String status,
            LocalDateTime orderedAt,
            Long price
    ) {
        public static List<OrderResponse> from(List<OrderResult> orders){
            List<OrderResponse> result = new ArrayList<>();
            for (OrderResult order : orders) {
                result.add(new OrderResponse(
                        order.orderId(),
                        order.status(),
                        order.orderedAt(),
                        order.price()
                ));
            }
            return result;
        }
    }

    public record OrderDetailResponse(
            Long orderId,
            String orderStatus,
            List<OrderItemDetailRep> items
    ) {
        public record OrderItemDetailRep(
                String productName,
                int quantity,
                BigDecimal originalPrice,
                BigDecimal discountedPrice
        ) {
            public static List<OrderItemDetailRep> from(List<OrderDetailResult.OrderItemDetail> details) {
                return  details.stream()
                        .map(item -> new OrderItemDetailRep(
                                item.productName(),
                                item.quantity(),
                                item.originalPrice(),
                                item.discountedPrice()
                        ))
                        .toList();
            }
        }

        public static OrderDetailResponse from(OrderDetailResult result) {
            return new OrderDetailResponse(
                    result.orderId(),
                    result.orderStatus(),
                    OrderItemDetailRep.from(result.items())
            );
        }
    }
}
