package com.loopers.application.payment.event;

import com.loopers.domain.payment.event.PaymentResultEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RecordApplicationEvents
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private ApplicationEvents events;

    @DisplayName("PaymentResultEvent가 올바르게 발행되고 리스너에서 수신된다")
    @Test
    void should_publish_and_receive_payment_result_event() {
        // Given
        Long orderId = 1L;
        String paymentSeq = "PAY_123";
        String transactionKey = "TXN_456";
        
        PaymentResultEvent event = PaymentResultEvent.createCompleted(orderId, paymentSeq, transactionKey);
        
        // When
        eventPublisher.publishEvent(event);
        
        // Then
        assertThat(events.stream(PaymentResultEvent.class).count()).isEqualTo(1);
        
        PaymentResultEvent publishedEvent = events.stream(PaymentResultEvent.class)
                .findFirst()
                .orElseThrow();
        
        assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(publishedEvent.getPaymentSeq()).isEqualTo(paymentSeq);
        assertThat(publishedEvent.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(publishedEvent.getResultType()).isEqualTo(PaymentResultEvent.PaymentResultType.COMPLETED);
    }

    @DisplayName("서로 다른 타입의 PaymentResultEvent들이 올바르게 구분된다")
    @Test
    void should_distinguish_different_payment_result_event_types() {
        // Given
        Long orderId = 1L;
        String paymentSeq = "PAY_123";
        
        PaymentResultEvent completedEvent = PaymentResultEvent.createCompleted(orderId, paymentSeq, "TXN_456");
        PaymentResultEvent failedEvent = PaymentResultEvent.createFailed(orderId, paymentSeq, "결제 실패");
        PaymentResultEvent cancelledEvent = PaymentResultEvent.createCancelled(orderId, paymentSeq, "결제 취소");
        
        // When
        eventPublisher.publishEvent(completedEvent);
        eventPublisher.publishEvent(failedEvent);
        eventPublisher.publishEvent(cancelledEvent);
        
        // Then
        assertThat(events.stream(PaymentResultEvent.class).count()).isEqualTo(3);
        
        long completedCount = events.stream(PaymentResultEvent.class)
                .filter(event -> event.getResultType() == PaymentResultEvent.PaymentResultType.COMPLETED)
                .count();
        long failedCount = events.stream(PaymentResultEvent.class)
                .filter(event -> event.getResultType() == PaymentResultEvent.PaymentResultType.FAILED)
                .count();
        long cancelledCount = events.stream(PaymentResultEvent.class)
                .filter(event -> event.getResultType() == PaymentResultEvent.PaymentResultType.CANCELLED)
                .count();
        
        assertThat(completedCount).isEqualTo(1);
        assertThat(failedCount).isEqualTo(1);
        assertThat(cancelledCount).isEqualTo(1);
    }
}
