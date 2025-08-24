package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Arrays;

public enum PaymentStatus {
    PENDING,           // 결제 대기
    PG_REQUEST_SENT,   // PG 요청 전송됨 (transactionKey 있음)
    PG_REQUEST_FAILED, // PG 요청 실패
    PROCESSING,        // 결제 처리중 (PG사에서 처리중)
    COMPLETED,         // 결제 완료
    FAILED,            // 결제 실패
    CANCELED,          // 결제 취소 (전액)
    PARTIAL_CANCELED,  // 부분 취소
    TIMEOUT_PENDING;   // 타임아웃으로 상태 확인 필요

    public static PaymentStatus from(String value) {
        validate(value);
        return PaymentStatus.valueOf(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제상태는 null이거나 빈 문자열일 수 없습니다.");
        } else {
            boolean exists = Arrays.stream(PaymentStatus.values())
                    .anyMatch(g -> g.name().equalsIgnoreCase(value));

            if (!exists) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제상태값이 올바르지 않습니다.");
            }
        }

    }
}
