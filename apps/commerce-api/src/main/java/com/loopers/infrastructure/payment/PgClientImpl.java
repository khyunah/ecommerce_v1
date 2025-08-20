package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PgPaymentRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgClientImpl implements PgClient {

    private final PgFeignClient pgFeignClient;

    @Override
    @CircuitBreaker(name = "pg-client", fallbackMethod = "fallbackPaymentRequest")
    @Retry(name = "pg-payment-request")
    @TimeLimiter(name = "pg-client")
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
        } catch (TimeoutException e) {
            log.error("PG 요청 타임아웃 - orderId: {}, timeout: 12s", request.orderId());
            return new PgPaymentResponse(
                    new PgPaymentResponse.PgMeta("FAIL", "Timeout", "결제 요청 시간이 초과되었습니다."),
                    null
            );
        } catch (Exception e) {
            log.error("PG 결제 요청 실패 - orderId: {}, error: {}", request.orderId(), e.getMessage(), e);
            // Retry 대상 예외는 다시 던져서 재시도하도록 함
            if (isRetryableException(e)) {
                throw e;
            }
            return new PgPaymentResponse(
                    new PgPaymentResponse.PgMeta("FAIL", "Network Error", "네트워크 오류가 발생했습니다."),
                    null
            );
        }
    }
    
    /**
     * Circuit Breaker OPEN 시 fallback 메서드
     */
    public PgPaymentResponse fallbackPaymentRequest(PgPaymentRequest request, Exception ex) {
        log.error("PG 결제 요청 fallback 실행 - orderId: {}, reason: {}", 
                  request.orderId(), ex.getMessage());
        
        return new PgPaymentResponse(
                new PgPaymentResponse.PgMeta(
                        "FAIL", 
                        "Circuit Breaker Open", 
                        "결제 시스템 일시 장애. 잠시 후 다시 시도해주세요."
                ),
                null
        );
    }
    
    /**
     * 재시도 가능한 예외인지 확인
     */
    private boolean isRetryableException(Exception e) {
        return e instanceof java.net.SocketTimeoutException ||
               e instanceof java.net.ConnectException ||
               e instanceof feign.RetryableException ||
               e instanceof PgFeignErrorDecoder.PgServiceUnavailableException;
    }
}
