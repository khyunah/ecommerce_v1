package com.loopers.domain.payment;

import com.loopers.domain.payment.dto.PgPaymentRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;

public interface PgClient {
    
    /**
     * PG사로 결제 요청을 전송합니다.
     * 
     * @param request PG 결제 요청 정보
     * @return PG 결제 응답 정보
     */
    PgPaymentResponse requestPayment(PgPaymentRequest request);
}
