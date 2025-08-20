package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByPaymentSeq(String paymentSeq) {
        return paymentJpaRepository.findByPaymentSeq(paymentSeq);
    }

    @Override
    public Optional<Payment> findByPgTid(String pgTid) {
        return paymentJpaRepository.findByPgTransactionKey(pgTid);
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        return paymentJpaRepository.findByRefOrderId(orderId);
    }

    @Override
    public List<Payment> findByPaymentStatus(PaymentStatus paymentStatus) {
        return paymentJpaRepository.findByPaymentStatus(paymentStatus);
    }

    @Override
    public List<Payment> findLongProcessingPayments(int minutes) {
        return paymentJpaRepository.findLongProcessingPayments(minutes);
    }

    @Override
    public void delete(Payment payment) {
        paymentJpaRepository.delete(payment);
    }
}
