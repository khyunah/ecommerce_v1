package com.loopers.infrastructure.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PgPaymentRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgClientImpl implements PgClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pg.payment.url:http://localhost:8081/api/payments}")
    private String pgPaymentUrl;

    @Override
    public PgPaymentResponse requestPayment(PgPaymentRequest request) {
        try {
            log.info("PG 결제 요청 시작 - orderId: {}, amount: {}", request.orderId(), request.amount());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<PgPaymentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PgPaymentResponse> response = restTemplate.exchange(
                    pgPaymentUrl,
                    HttpMethod.POST,
                    entity,
                    PgPaymentResponse.class
            );
            
            PgPaymentResponse pgResponse = response.getBody();
            log.info("PG 결제 응답 - orderId: {}, result: {}", 
                    request.orderId(), pgResponse.meta().result());
            
            return pgResponse;
            
        } catch (Exception e) {
            log.error("PG 결제 요청 실패 - orderId: {}, error: {}", request.orderId(), e.getMessage(), e);
            
            // 실패 응답 반환
            return new PgPaymentResponse(
                    new PgPaymentResponse.PgMeta(
                            "FAIL",
                            "NETWORK_ERROR",
                            "PG 결제 요청 실패: " + e.getMessage()
                    ),
                    null
            );
        }
    }
}
