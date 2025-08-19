package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Arrays;

public enum OrderStatus {
    PENDING,        // 결제 대기 (주문 생성 후 결제 전)
    PAID,           // 결제 완료
    PREPARING,      // 상품 준비중
    SHIPPED,        // 배송중
    DELIVERED,      // 배송 완료
    CANCELED,       // 주문 취소
    REFUND_REQUESTED, // 환불 요청
    REFUNDED;        // 환불 완료

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
