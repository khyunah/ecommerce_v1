package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Arrays;

public enum OrderStatus {
    ORDERED,        // 주문 완료 (고객이 주문을 완료한 상태)
    PAID,           // 결제 완료
    PACKING,        // 상품 준비 중 (포장, 출고 준비)
    SHIPPED,        // 배송 중
    DELIVERED,      // 배송 완료 (고객에게 상품 전달됨)
    CANCELLED,      // 주문 취소됨
    RETURN_REQUESTED, // 반품 요청됨
    RETURNED,       // 반품 완료
    EXCHANGE_REQUESTED, // 교환 요청됨
    EXCHANGED,      // 교환 완료
    FAILED;          // 결제 실패 또는 주문 실패

    public static OrderStatus from(String value) {
        validate(value);
        return OrderStatus.valueOf(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문상태는 null이거나 빈 문자열일 수 없습니다.");
        } else {
            boolean exists = Arrays.stream(OrderStatus.values())
                    .anyMatch(g -> g.name().equalsIgnoreCase(value));

            if (!exists) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문상태값이 올바르지 않습니다.");
            }
        }

    }

}
