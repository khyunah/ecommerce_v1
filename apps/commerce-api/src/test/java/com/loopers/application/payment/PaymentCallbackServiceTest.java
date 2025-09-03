package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.PgCallbackRequest;
import com.loopers.domain.payment.event.PaymentResultEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentCallbackServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentStatusService paymentStatusService;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private PaymentCallbackService paymentCallbackService;

    @DisplayName("결제 완료 콜백 처리 시 결제 완료 이벤트가 발행된다")
    @Test
    void should_publish_payment_completed_event_when_payment_callback_completed() {
        // Given
        String paymentSeq = "PAY_123";
        String transactionKey = "TXN_456";
        Long orderId = 1L;
        Long amount = 10000L;
        
        Payment payment = createMockPayment(orderId, paymentSeq, PaymentStatus.PENDING);
        PgCallbackRequest request = new PgCallbackRequest(
                paymentSeq,     // orderId (실제로는 paymentSeq)
                transactionKey, // transactionKey
                "COMPLETED",    // status
                amount,         // amount
                null            // message
        );
        
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentCallbackService.processCallback(request);
        
        // Then
        // 결제 상태 업데이트 확인
        verify(paymentRepository).save(any(Payment.class));
        
        // 이벤트 발행 확인
        ArgumentCaptor<PaymentResultEvent> eventCaptor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        PaymentResultEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(publishedEvent.getPaymentSeq()).isEqualTo(paymentSeq);
        assertThat(publishedEvent.getResultType()).isEqualTo(PaymentResultEvent.PaymentResultType.COMPLETED);
        assertThat(publishedEvent.getTransactionKey()).isEqualTo(transactionKey);
    }

    @DisplayName("결제 실패 콜백 처리 시 결제 실패 이벤트가 발행된다")
    @Test
    void should_publish_payment_failed_event_when_payment_callback_failed() {
        // Given
        String paymentSeq = "PAY_123";
        String errorMessage = "카드 한도 초과";
        Long orderId = 1L;
        Long amount = 10000L;
        
        Payment payment = createMockPayment(orderId, paymentSeq, PaymentStatus.PENDING);
        PgCallbackRequest request = new PgCallbackRequest(
                paymentSeq,     // orderId (실제로는 paymentSeq)
                null,           // transactionKey (실패 시 null)
                "FAILED",       // status
                amount,         // amount
                errorMessage    // message
        );
        
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentCallbackService.processCallback(request);
        
        // Then
        ArgumentCaptor<PaymentResultEvent> eventCaptor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        PaymentResultEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(publishedEvent.getPaymentSeq()).isEqualTo(paymentSeq);
        assertThat(publishedEvent.getResultType()).isEqualTo(PaymentResultEvent.PaymentResultType.FAILED);
        assertThat(publishedEvent.getMessage()).isEqualTo(errorMessage);
    }

    @DisplayName("이미 처리된 결제에 대해서는 이벤트를 발행하지 않는다")
    @Test
    void should_not_publish_event_when_payment_already_processed() {
        // Given
        String paymentSeq = "PAY_123";
        Long orderId = 1L;
        Long amount = 10000L;
        
        Payment payment = createMockPayment(orderId, paymentSeq, PaymentStatus.COMPLETED);
        PgCallbackRequest request = new PgCallbackRequest(
                paymentSeq,     // orderId (실제로는 paymentSeq)
                "TXN_456",      // transactionKey
                "COMPLETED",    // status
                amount,         // amount
                null            // message
        );
        
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentCallbackService.processCallback(request);
        
        // Then
        // 이벤트 발행되지 않음
        verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(any());
    }

    private Payment createMockPayment(Long orderId, String paymentSeq, PaymentStatus status) {
        // 실제 Payment 객체 생성 (mock 대신)
        Payment payment = Payment.create(orderId, paymentSeq, "CARD", 10000L, "TOSS");
        // 상태만 변경
        if (status == PaymentStatus.COMPLETED) {
            payment.completePayment();
        }
        return payment;
    }
}
