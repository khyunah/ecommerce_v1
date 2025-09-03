package com.loopers.application.payment;

import com.loopers.application.order.OrderRecoveryService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.PgCallbackRequest;
import com.loopers.domain.payment.event.PaymentResultEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RecordApplicationEvents
class PaymentCallbackIntegrationTest {

    @Autowired
    private PaymentCallbackService paymentCallbackService;
    
    @Autowired
    private ApplicationEvents events;

    @MockBean
    private PaymentRepository paymentRepository;
    
    @MockBean
    private OrderRepository orderRepository;
    
    @MockBean
    private PaymentStatusService paymentStatusService;
    
    @MockBean
    private OrderRecoveryService orderRecoveryService;

    @DisplayName("결제 완료 콜백 처리 시 이벤트가 발행된다")
    @Test
    void should_publish_payment_completed_event_when_callback_received() {
        // Given
        String paymentSeq = "PAY_123";
        String transactionKey = "TXN_456";
        Long orderId = 1L;
        Long amount = 10000L;
        
        Payment payment = createPayment(orderId, paymentSeq, PaymentStatus.PENDING);
        PgCallbackRequest request = new PgCallbackRequest(
                paymentSeq,
                transactionKey,
                "COMPLETED",
                amount,
                null
        );
        
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentCallbackService.processCallback(request);
        
        // Then
        // PaymentResultEvent 발행 확인
        long eventCount = events.stream(PaymentResultEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        
        // 발행된 이벤트 내용 검증
        PaymentResultEvent publishedEvent = events.stream(PaymentResultEvent.class)
                .findFirst()
                .orElseThrow();
        
        assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(publishedEvent.getPaymentSeq()).isEqualTo(paymentSeq);
        assertThat(publishedEvent.getResultType()).isEqualTo(PaymentResultEvent.PaymentResultType.COMPLETED);
        assertThat(publishedEvent.getTransactionKey()).isEqualTo(transactionKey);
        
        // Payment 저장 확인
        verify(paymentRepository).save(payment);
    }

    @DisplayName("결제 실패 콜백 처리 시 이벤트가 발행된다")
    @Test
    void should_publish_payment_failed_event_when_callback_received() {
        // Given
        String paymentSeq = "PAY_123";
        String errorMessage = "카드 한도 초과";
        Long orderId = 1L;
        Long amount = 10000L;
        
        Payment payment = createPayment(orderId, paymentSeq, PaymentStatus.PENDING);
        PgCallbackRequest request = new PgCallbackRequest(
                paymentSeq,
                null,
                "FAILED",
                amount,
                errorMessage
        );
        
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentCallbackService.processCallback(request);
        
        // Then
        // PaymentResultEvent 발행 확인
        long eventCount = events.stream(PaymentResultEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        
        // 발행된 이벤트 내용 검증
        PaymentResultEvent publishedEvent = events.stream(PaymentResultEvent.class)
                .findFirst()
                .orElseThrow();
        
        assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(publishedEvent.getPaymentSeq()).isEqualTo(paymentSeq);
        assertThat(publishedEvent.getResultType()).isEqualTo(PaymentResultEvent.PaymentResultType.FAILED);
        assertThat(publishedEvent.getMessage()).isEqualTo(errorMessage);
        
        // Payment 저장 확인
        verify(paymentRepository).save(payment);
    }

    @DisplayName("이미 처리된 결제에 대해서는 이벤트를 발행하지 않는다")
    @Test
    void should_not_publish_event_when_payment_already_completed() {
        // Given
        String paymentSeq = "PAY_123";
        Long orderId = 1L;
        
        Payment payment = createPayment(orderId, paymentSeq, PaymentStatus.COMPLETED);
        PgCallbackRequest request = new PgCallbackRequest(
                paymentSeq,
                "TXN_456",
                "COMPLETED",
                10000L,
                null
        );
        
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentCallbackService.processCallback(request);
        
        // Then
        // 이벤트가 발행되지 않음
        long eventCount = events.stream(PaymentResultEvent.class).count();
        assertThat(eventCount).isEqualTo(0);
    }

    private Payment createPayment(Long orderId, String paymentSeq, PaymentStatus status) {
        Payment payment = Payment.create(orderId, paymentSeq, "CARD", 10000L, "TOSS");
        if (status == PaymentStatus.COMPLETED) {
            payment.completePayment();
        }
        return payment;
    }
}
