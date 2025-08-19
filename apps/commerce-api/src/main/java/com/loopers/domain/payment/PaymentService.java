package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 결제요청
    public Payment createPayment(Long orderId, String paymentMethod,
                                 Long paymentAmount, String pgProvider) {
        String paymentSeq = generatePaymentSeq();

        Payment payment = Payment.create(orderId, paymentSeq, paymentMethod,
                paymentAmount, pgProvider);

        return paymentRepository.save(payment);
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

    // 결제 번호 생성
    private String generatePaymentSeq() {
        return "KHY_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
