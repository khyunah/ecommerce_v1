package com.loopers.domain.payment.dto;

/**
 * PG사에서 보내는 콜백 요청 DTO
 */
public record PgCallbackRequest(
        String orderId,         // 주문 ID (paymentSeq)
        String transactionKey,  // PG사 거래 키
        String status,          // 결제 상태 (COMPLETED, FAILED, CANCELED)
        Long amount,            // 결제 금액
        String message          // 메시지
) {
}
