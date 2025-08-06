package com.loopers.application.order.out;

import com.loopers.domain.order.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record OrderDetailResult(
        Long orderId,
        String orderStatus,
        List<OrderItemDetail> items
) {
    public record OrderItemDetail(
            String productName,
            int quantity,
            BigDecimal originalPrice,
            BigDecimal discountedPrice
    ) {
        public static List<OrderItemDetail> from(Order order) {
            return  order.getOrderItems().stream()
                    .map(item -> new OrderDetailResult.OrderItemDetail(
                            item.getProductName(),
                            item.getQuantity(),
                            item.getOriginalPrice().getValue(),
                            item.getSellingPrice().getValue()
                    ))
                    .toList();
        }
    }

    public static OrderDetailResult from(Order order) {
        return new OrderDetailResult(
                order.getId(),
                order.getOrderStatus().name(),
                OrderItemDetail.from(order)
        );
    }
}
