package com.loopers.domain.payment;

import com.loopers.domain.payment.dto.PgPaymentRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;

    @Value("${pg.callback.url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    // 결제요청
    public Payment createPayment(Long orderId, String paymentMethod,
                                 Long paymentAmount, String pgProvider) {
        String paymentSeq = generatePaymentSeq();

        Payment payment = Payment.create(orderId, paymentSeq, paymentMethod,
                paymentAmount, pgProvider);

        return paymentRepository.save(payment);
    }

    // PG사 결제 요청
    public PgPaymentResponse requestPgPayment(Payment payment, String cardType, String cardNo) {
        log.info("PG 결제 요청 - paymentSeq: {}, amount: {}", payment.getPaymentSeq(), payment.getPaymentAmount());
        
        PgPaymentRequest request = PgPaymentRequest.create(
                payment.getPaymentSeq(),  // orderId로 paymentSeq 사용
                cardType,
                cardNo,
                payment.getPaymentAmount(),
                callbackUrl
        );
        
        return pgClient.requestPayment(request);
    }

    // PG 요청 성공 시 transactionKey 저장
    public void updateTransactionKey(String paymentSeq, String transactionKey) {
        Payment payment = paymentRepository.findByPaymentSeq(paymentSeq)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.setPgTransactionKey(transactionKey);
        paymentRepository.save(payment);
    }

    // 결제 완료 처리
    public void completePayment(String paymentSeq, String pgTid) {
        Payment payment = paymentRepository.findByPaymentSeq(paymentSeq)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.completePayment(pgTid);
        paymentRepository.save(payment);
    }

    // 결제 실패 처리
    public void failPayment(String paymentSeq, String failureReason) {
        Payment payment = paymentRepository.findByPaymentSeq(paymentSeq)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.failPayment(failureReason);
        paymentRepository.save(payment);
    }

    // 결제 취소
    public void cancelPayment(String paymentSeq, String cancelReason) {
        Payment payment = paymentRepository.findByPaymentSeq(paymentSeq)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.cancelPayment(cancelReason);
        paymentRepository.save(payment);
    }

    // 결제 정보 조회
    public Payment getPayment(String paymentSeq) {
        return paymentRepository.findByPaymentSeq(paymentSeq)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
    }

    // 주문 ID로 결제 목록 조회
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // 특정 상태의 결제 목록 조회
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    // 결제 정보 저장
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    // 결제 번호 생성
    private String generatePaymentSeq() {
        return "KHY_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
