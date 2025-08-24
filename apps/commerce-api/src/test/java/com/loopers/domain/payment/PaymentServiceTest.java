package com.loopers.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @DisplayName("결제 생성이 정상적으로 작동한다")
    @Test
    void createPayment_works_correctly() {
        // given
        Long orderId = 123L;
        String paymentMethod = "CARD";
        Long paymentAmount = 1000L;
        String pgProvider = "KHY_PG";

        // when
        Payment payment = paymentService.createPayment(orderId, paymentMethod, paymentAmount, pgProvider);

        // then
        assertThat(payment).isNotNull();
        assertThat(payment.getRefOrderId()).isEqualTo(orderId);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(payment.getPaymentAmount()).isEqualTo(paymentAmount);
        assertThat(payment.getPgProvider()).isEqualTo(pgProvider);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);

        // 데이터베이스에서 조회 가능한지 확인
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getId()).isEqualTo(payment.getId());
    }

    @DisplayName("여러 결제 방법으로 결제 생성이 가능하다")
    @Test
    void createPayment_with_different_payment_methods() {
        // given
        Long orderId = 456L;

        // when
        Payment cardPayment = paymentService.createPayment(orderId, "CARD", 1000L, "KHY_PG");
        Payment pointPayment = paymentService.createPayment(orderId, "POINT_ONLY", 0L, "KHY_PG");

        // then
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        assertThat(payments).hasSize(2);

        assertThat(cardPayment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(pointPayment.getPaymentMethod()).isEqualTo(PaymentMethod.POINT_ONLY);
    }
}
