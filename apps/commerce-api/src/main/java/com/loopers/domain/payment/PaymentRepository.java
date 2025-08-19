package com.loopers.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    // 결제 정보 저장
    Payment save(Payment payment);

    // 결제 번호로 조회
    Optional<Payment> findByPaymentSeq(String paymentSeq);

    // PG사 거래 ID로 조회
    Optional<Payment> findByPgTid(String pgTid);

    // 주문 ID로 결제 목록 조회
    List<Payment> findByOrderId(Long orderId);

    // 특정 상태의 결제 목록 조회
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    // 결제 정보 삭제 (실제로는 사용하지 않을 것 같지만 완성도를 위해)
    void delete(Payment payment);
}
