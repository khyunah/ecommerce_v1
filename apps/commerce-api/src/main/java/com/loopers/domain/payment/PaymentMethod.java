package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Arrays;

public enum PaymentMethod {
    CARD,           // 카드
    POINT_ONLY;      // 포인트만 사용 (PG사 결제 없음)

    public static PaymentMethod from(String value) {
        validate(value);
        return PaymentMethod.valueOf(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제수단은 null이거나 빈 문자열일 수 없습니다.");
        } else {
            boolean exists = Arrays.stream(PaymentMethod.values())
                    .anyMatch(g -> g.name().equalsIgnoreCase(value));

            if (!exists) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제수단값이 올바르지 않습니다.");
            }
        }
    }

    /**
     * PG사 연결이 필요한 결제 방법인지 판단
     * 현재는 CARD만 PG사 연결이 필요함
     */
    public boolean requiresPgConnection() {
        return this == CARD;
    }
}
