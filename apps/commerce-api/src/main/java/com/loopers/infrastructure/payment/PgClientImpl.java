package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PgPaymentRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgClientImpl implements PgClient {

    private final PgFeignClient pgFeignClient;

    @Override
    public PgPaymentResponse requestPayment(PgPaymentRequest request) {
        try {
            log.info("PG 결제 요청 시작 - orderId: {}, amount: {}", request.orderId(), request.amount());
            
            PgPaymentResponse response = pgFeignClient.requestPayment(request);
            
            log.info("PG 결제 응답 - orderId: {}, result: {}", 
                    request.orderId(), response.meta().result());
            
            return response;
            
        } catch (PgFeignErrorDecoder.PgBadRequestException e) {
            log.error("PG 요청 형식 오류 - orderId: {}, error: {}", request.orderId(), e.getMessage());
            return new PgPaymentResponse(
                    new PgPaymentResponse.PgMeta("FAIL", "Bad Request", e.getMessage()),
                    null
            );
        } catch (PgFeignErrorDecoder.PgServerException e) {
            log.error("PG 서버 오류 - orderId: {}, error: {}", request.orderId(), e.getMessage());
            return new PgPaymentResponse(
                    new PgPaymentResponse.PgMeta("FAIL", "Internal Server Error", e.getMessage()),
                    null
            );
        } catch (Exception e) {
            log.error("PG 결제 요청 실패 - orderId: {}, error: {}", request.orderId(), e.getMessage(), e);
            return new PgPaymentResponse(
                    new PgPaymentResponse.PgMeta("FAIL", "Network Error", "네트워크 오류가 발생했습니다."),
                    null
            );
        }
    }
}
