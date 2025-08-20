package com.loopers.domain.payment.dto;

public record PgPaymentRequest(
        String orderId,
        String cardType,
        String cardNo,
        String amount,
        String callbackUrl
) {
    public static PgPaymentRequest create(String paymentSeq, String cardType, String cardNo, Long amount, String callbackUrl) {
        return new PgPaymentRequest(
                paymentSeq,
                cardType,
                cardNo,
                amount.toString(),
                callbackUrl
        );
    }
}
