package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.PgCallbackRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import com.loopers.domain.payment.event.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentCallbackService {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusService paymentStatusService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * PG 콜백 처리 - 이벤트 기반으로 분리
     */
    @Transactional
    public void processCallback(PgCallbackRequest request) {
        log.info("콜백 처리 시작 - orderId: {}, status: {}", request.orderId(), request.status());
        
        try {

            // 결제 정보 조회
            Payment payment = paymentRepository.findByPaymentSeq(request.orderId())
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + request.orderId()));
            
            // 이미 처리된 콜백인지 확인 (중복 처리 방지)
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || 
                payment.getPaymentStatus() == PaymentStatus.CANCELED) {
                log.info("이미 처리된 결제 - paymentSeq: {}, status: {}", 
                        payment.getPaymentSeq(), payment.getPaymentStatus());
                return;
            }
            
            // 콜백 상태에 따른 결제 상태 업데이트
            switch (request.status()) {
                case "COMPLETED" -> {
                    payment.completePayment(request.transactionKey());
                    paymentRepository.save(payment);
                    log.info("결제 완료 상태 업데이트 완료 - paymentSeq: {}", payment.getPaymentSeq());
                    
                    // 결제 완료 이벤트 발행 (부가 로직과 분리)
                    publishPaymentResultEvent(PaymentResultEvent.createCompleted(
                            payment.getRefOrderId(), payment.getPaymentSeq(), request.transactionKey()));
                }
                case "FAILED" -> {
                    payment.failPayment(request.message());
                    paymentRepository.save(payment);
                    log.info("결제 실패 상태 업데이트 완료 - paymentSeq: {}", payment.getPaymentSeq());
                    
                    // 결제 실패 이벤트 발행 (부가 로직과 분리)
                    publishPaymentResultEvent(PaymentResultEvent.createFailed(
                            payment.getRefOrderId(), payment.getPaymentSeq(), request.message()));
                }
                case "CANCELED" -> {
                    payment.cancelPayment(request.message());
                    paymentRepository.save(payment);
                    log.info("결제 취소 상태 업데이트 완료 - paymentSeq: {}", payment.getPaymentSeq());
                    
                    // 결제 취소 이벤트 발행 (부가 로직과 분리)
                    publishPaymentResultEvent(PaymentResultEvent.createCancelled(
                            payment.getRefOrderId(), payment.getPaymentSeq(), request.message()));
                }
                default -> {
                    log.warn("알 수 없는 콜백 상태 - paymentSeq: {}, status: {}", 
                            payment.getPaymentSeq(), request.status());
                }
            }
            
        } catch (Exception e) {
            log.error("콜백 처리 중 오류 발생 - orderId: {}", request.orderId(), e);
            throw e; // 콜백 처리 실패는 재전송을 위해 예외 전파
        }
    }

    /**
     * 결제 결과 이벤트 발행 - 부가 로직과 분리
     */
    private void publishPaymentResultEvent(PaymentResultEvent event) {
        try {
            log.info("결제 결과 이벤트 발행 - orderId: {}, resultType: {}", 
                    event.getOrderId(), event.getResultType());
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // 이벤트 발행 실패해도 결제 처리는 성공으로 처리
            log.error("결제 결과 이벤트 발행 실패 - orderId: {}", event.getOrderId(), e);
        }
    }

    /**
     * 수동 결제 상태 확인
     */
    @Transactional
    public String manualStatusCheck(String paymentSeq) {
        log.info("수동 결제 상태 확인 - paymentSeq: {}", paymentSeq);
        
        // 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentSeq(paymentSeq)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentSeq));
        
        // 현재 상태가 완료/취소인 경우 확인 불필요
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || 
            payment.getPaymentStatus() == PaymentStatus.CANCELED || 
            payment.getPaymentStatus() == PaymentStatus.FAILED) {
            return "이미 처리 완료된 결제입니다. 상태: " + payment.getPaymentStatus();
        }
        
        try {
            // PG사에서 상태 확인
            PgPaymentResponse pgResponse = paymentStatusService.getPaymentStatus(paymentSeq);
            
            if (pgResponse.isSuccess() && pgResponse.data() != null) {
                // 이전 상태 저장
                PaymentStatus previousStatus = payment.getPaymentStatus();
                
                // 상태 동기화
                paymentStatusService.syncPaymentStatus(payment, pgResponse);
                
                // 상태가 변경된 경우 이벤트 발행
                if (payment.getPaymentStatus() == PaymentStatus.COMPLETED && previousStatus != PaymentStatus.COMPLETED) {
                    publishPaymentResultEvent(PaymentResultEvent.createCompleted(
                            payment.getRefOrderId(), payment.getPaymentSeq(), null));
                } else if ((payment.getPaymentStatus() == PaymentStatus.FAILED || payment.getPaymentStatus() == PaymentStatus.CANCELED)
                          && (previousStatus != PaymentStatus.FAILED && previousStatus != PaymentStatus.CANCELED)) {
                    publishPaymentResultEvent(PaymentResultEvent.createFailed(
                            payment.getRefOrderId(), payment.getPaymentSeq(), "수동 상태 확인 결과"));
                }
                
                return String.format("상태 확인 완료. %s → %s", 
                                   previousStatus, payment.getPaymentStatus());
                
            } else {
                return "PG사 상태 확인 실패: " + pgResponse.meta().message();
            }
            
        } catch (Exception e) {
            log.error("수동 상태 확인 중 오류 - paymentSeq: {}", paymentSeq, e);
            return "상태 확인 중 오류 발생: " + e.getMessage();
        }
    }
}
