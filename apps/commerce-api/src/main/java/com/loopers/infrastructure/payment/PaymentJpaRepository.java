package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    // 결제 번호로 조회
    Optional<Payment> findByPaymentSeq(String paymentSeq);

    // PG사 거래 ID로 조회
    Optional<Payment> findByPgTransactionKey(String pgTransactionKey);

    // 주문 ID로 조회
    List<Payment> findByRefOrderId(Long refOrderId);

    // 결제 상태로 조회
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

}
