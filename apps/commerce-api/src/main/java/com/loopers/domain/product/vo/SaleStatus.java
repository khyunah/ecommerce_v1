package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Arrays;

public enum SaleStatus {
    ON_SALE,        // 판매 중
    SOLD_OUT,       // 품절
    STOP_SELLING,   // 판매 중지 (관리자 수동 중단)
    PREORDER,       // 예약 판매
    DISCONTINUED;   // 단종 (더 이상 판매하지 않음)

    public static SaleStatus from(String value) {
        validate(value);
        return SaleStatus.valueOf(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "SaleStatus는 null이거나 빈 문자열일 수 없습니다.");
        } else {
            boolean exists = Arrays.stream(SaleStatus.values())
                    .anyMatch(g -> g.name().equalsIgnoreCase(value));

            if (!exists) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용 가능한 SaleStatus가 아닙니다.");
            }
        }

    }
}
