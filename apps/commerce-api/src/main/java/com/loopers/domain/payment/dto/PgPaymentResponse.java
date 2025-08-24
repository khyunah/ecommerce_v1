package com.loopers.domain.payment.dto;

public record PgPaymentResponse(
        PgMeta meta,
        PgData data
) {
    public record PgMeta(
            String result,
            String errorCode,
            String message
    ) {}
    
    public record PgData(
            String transactionKey,
            String status
    ) {}
    
    public boolean isSuccess() {
        return "SUCCESS".equals(meta.result());
    }
    
    public String getTransactionKey() {
        return data != null ? data.transactionKey() : null;
    }
    
    public String getErrorMessage() {
        return meta.message();
    }
}
