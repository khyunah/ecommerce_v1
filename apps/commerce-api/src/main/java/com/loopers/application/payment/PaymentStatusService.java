package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.PgFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentStatusService {

    private final PaymentRepository paymentRepository;
    private final PgFeignClient pgFeignClient;

    /**
     * PG사에서 결제 상태 조회 (즉시 확인용)
     */
    @Retry(name = "pg-payment-request")
    @CircuitBreaker(name = "pg-client", fallbackMethod = "fallbackGetPaymentStatus")
    public PgPaymentResponse getPaymentStatus(String orderId) {
        try {
            System.out.println("PG 결제 상태 확인 요청 - orderId: " + orderId);
            
            PgPaymentResponse response = pgFeignClient.getPaymentStatus(orderId);
            
            System.out.println("PG 결제 상태 응답 - orderId: " + orderId + ", status: " + 
                             (response.data() != null ? response.data().status() : "NO_DATA"));
            
            return response;
        } catch (Exception e) {
            System.out.println("PG 결제 상태 확인 실패 - orderId: " + orderId + ", error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 타임아웃/Circuit Breaker 실패한 결제건 상태 확인 (3초 간격으로 3번 재시도)
     */
    @Retry(name = "pg-payment-status-check", fallbackMethod = "finalFailureHandler")
    @CircuitBreaker(name = "pg-client")
    public void checkAndRecoverPaymentStatus(Payment payment) {
        try {
            System.out.println("결제 상태 복구 시도 - paymentSeq: " + payment.getPaymentSeq() + 
                             " (3초 간격 재시도)");
            
            PgPaymentResponse response = pgFeignClient.getPaymentStatus(payment.getPaymentSeq());
            
            if (response.isSuccess() && response.data() != null) {
                // PG 상태 확인 성공 - 상태 동기화
                syncPaymentStatus(payment, response);
                System.out.println("결제 상태 복구 성공 - paymentSeq: " + payment.getPaymentSeq());
            } else {
                // PG 응답이 실패인 경우 재시도를 위해 예외 발생
                throw new RuntimeException("PG 상태 확인 응답 실패: " + response.meta().message());
            }
            
        } catch (Exception e) {
            System.out.println("결제 상태 복구 실패 - paymentSeq: " + payment.getPaymentSeq() + 
                             ", error: " + e.getMessage());
            throw e; // 재시도를 위해 예외 재발생
        }
    }

    /**
     * Circuit Breaker OPEN 시 fallback 메서드
     */
    public PgPaymentResponse fallbackGetPaymentStatus(String orderId, Exception ex) {
        System.out.println("PG 결제 상태 확인 fallback 실행 - orderId: " + orderId + ", reason: " + ex.getMessage());
        
        return new PgPaymentResponse(
                new PgPaymentResponse.PgMeta("FAIL", "Circuit Breaker Open", "결제 상태 확인 시스템 일시 장애"),
                null
        );
    }

    /**
     * 3번 재시도 후 최종 실패 처리 (fallback 메서드)
     * 총 9초 후(3초×3번) 자동 취소 처리
     */
    public void finalFailureHandler(Payment payment, Exception ex) {
        System.out.println("결제 상태 확인 최종 실패 - 취소 처리: " + payment.getPaymentSeq());
        
        // 3번 재시도 실패 시 결제 취소 처리
        payment.updateStatus(PaymentStatus.FAILED);
        payment.failPayment("PG 상태 확인 3번 실패로 자동 취소");
        paymentRepository.save(payment);
        
        // 재고/포인트 복구는 별도 서비스에서 처리
        System.out.println("재고/포인트 복구가 필요한 결제: " + payment.getPaymentSeq());
    }

    /**
     * TIMEOUT_PENDING 상태인 결제건들 조회
     */
    public List<Payment> findTimeoutPendingPayments() {
        return paymentRepository.findByPaymentStatus(PaymentStatus.TIMEOUT_PENDING);
    }

    /**
     * 장시간 PROCESSING 상태인 결제건들 조회 (30분 이상)
     */
    public List<Payment> findLongProcessingPayments() {
        return paymentRepository.findLongProcessingPayments(30); // 30분 이상
    }

    /**
     * 결제 상태를 PG사 응답에 따라 동기화
     */
    @Transactional
    public void syncPaymentStatus(Payment payment, PgPaymentResponse pgResponse) {
        if (pgResponse.isSuccess() && pgResponse.data() != null) {
            String pgStatus = pgResponse.data().status();
            
            switch (pgStatus) {
                case "COMPLETED" -> {
                    payment.completePayment();
                    System.out.println("결제 완료로 상태 변경 - paymentSeq: " + payment.getPaymentSeq());
                }
                case "FAILED" -> {
                    payment.failPayment("PG사에서 결제 실패 처리");
                    System.out.println("결제 실패로 상태 변경 - paymentSeq: " + payment.getPaymentSeq());
                }
                case "PENDING", "PROCESSING" -> {
                    payment.updateStatus(PaymentStatus.PROCESSING);
                    System.out.println("결제 처리중으로 상태 변경 - paymentSeq: " + payment.getPaymentSeq());
                }
                case "CANCELED" -> {
                    payment.cancelPayment();
                    System.out.println("결제 취소로 상태 변경 - paymentSeq: " + payment.getPaymentSeq());
                }
                default -> {
                    System.out.println("알 수 없는 PG 상태 - paymentSeq: " + payment.getPaymentSeq() + ", status: " + pgStatus);
                }
            }
            
            paymentRepository.save(payment);
        } else {
            System.out.println("PG 상태 확인 실패 - paymentSeq: " + payment.getPaymentSeq());
        }
    }
}
