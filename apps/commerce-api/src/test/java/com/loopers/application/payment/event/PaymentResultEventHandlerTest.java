package com.loopers.application.payment.event;

import com.loopers.application.order.OrderRecoveryService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.event.PaymentResultEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentResultEventHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private OrderRecoveryService orderRecoveryService;
    
    @InjectMocks
    private PaymentResultEventHandler paymentResultEventHandler;

    @DisplayName("결제 완료 이벤트 처리 시 주문 상태가 완료로 변경된다")
    @Test
    void should_complete_order_when_payment_completed_event_received() {
        // Given
        Long orderId = 1L;
        String paymentSeq = "PAY_123";
        String transactionKey = "TXN_456";
        
        Order order = mock(Order.class);
        PaymentResultEvent event = PaymentResultEvent.createCompleted(orderId, paymentSeq, transactionKey);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        
        // When
        paymentResultEventHandler.handlePaymentResultEvent(event);
        
        // Then
        verify(order).completePayment();
        verify(orderRepository).save(order);
    }

    @DisplayName("결제 실패 이벤트 처리 시 주문 복구 서비스가 호출된다")
    @Test
    void should_call_recovery_service_when_payment_failed_event_received() {
        // Given
        Long orderId = 1L;
        String paymentSeq = "PAY_123";
        String message = "결제 실패";
        
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);
        PaymentResultEvent event = PaymentResultEvent.createFailed(orderId, paymentSeq, message);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentResultEventHandler.handlePaymentResultEvent(event);
        
        // Then
        verify(orderRecoveryService).handlePaymentFailure(payment, order);
    }

    @DisplayName("결제 취소 이벤트 처리 시 주문 복구 서비스가 호출된다")
    @Test
    void should_call_recovery_service_when_payment_cancelled_event_received() {
        // Given
        Long orderId = 1L;
        String paymentSeq = "PAY_123";
        String message = "결제 취소";
        
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);
        PaymentResultEvent event = PaymentResultEvent.createCancelled(orderId, paymentSeq, message);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(paymentRepository.findByPaymentSeq(paymentSeq)).willReturn(Optional.of(payment));
        
        // When
        paymentResultEventHandler.handlePaymentResultEvent(event);
        
        // Then
        verify(orderRecoveryService).handlePaymentFailure(payment, order);
    }

    @DisplayName("존재하지 않는 주문에 대한 이벤트 처리 시 복구 서비스가 호출되지 않는다")
    @Test
    void should_not_call_recovery_service_when_order_not_found() {
        // Given
        Long orderId = 999L;
        String paymentSeq = "PAY_123";
        PaymentResultEvent event = PaymentResultEvent.createCompleted(orderId, paymentSeq, "TXN_456");
        
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());
        
        // When
        paymentResultEventHandler.handlePaymentResultEvent(event);
        
        // Then
        // 복구 서비스가 호출되지 않음을 확인
        verify(orderRecoveryService, never()).handlePaymentFailure(any(), any());
    }
}
