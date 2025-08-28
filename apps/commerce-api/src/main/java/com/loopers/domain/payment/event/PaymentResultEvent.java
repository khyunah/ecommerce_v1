package com.loopers.domain.payment.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 결과 이벤트
 * 결제 상태 업데이트와 주문 처리를 분리하기 위한 이벤트
 */
@Getter
public class PaymentResultEvent {
    private final Long orderId;
    private final String paymentSeq;
    private final PaymentResultType resultType;
    private final String transactionKey;
    private final String message;
    private final LocalDateTime occurredAt;

    private PaymentResultEvent(Long orderId, String paymentSeq, PaymentResultType resultType, 
                              String transactionKey, String message) {
        this.orderId = orderId;
        this.paymentSeq = paymentSeq;
        this.resultType = resultType;
        this.transactionKey = transactionKey;
        this.message = message;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * 결제 완료 이벤트
     */
    public static PaymentResultEvent createCompleted(Long orderId, String paymentSeq, String transactionKey) {
        return new PaymentResultEvent(orderId, paymentSeq, PaymentResultType.COMPLETED, transactionKey, null);
    }

    /**
     * 결제 실패 이벤트
     */
    public static PaymentResultEvent createFailed(Long orderId, String paymentSeq, String message) {
        return new PaymentResultEvent(orderId, paymentSeq, PaymentResultType.FAILED, null, message);
    }

    /**
     * 결제 취소 이벤트
     */
    public static PaymentResultEvent createCancelled(Long orderId, String paymentSeq, String message) {
        return new PaymentResultEvent(orderId, paymentSeq, PaymentResultType.CANCELLED, null, message);
    }

    public enum PaymentResultType {
        COMPLETED,  // 결제 완료
        FAILED,     // 결제 실패
        CANCELLED   // 결제 취소
    }
}
