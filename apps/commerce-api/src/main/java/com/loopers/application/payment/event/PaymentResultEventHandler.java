package com.loopers.application.payment.event;

import com.loopers.application.order.OrderRecoveryService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.event.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultEventHandler {
    
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRecoveryService orderRecoveryService;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 별도 트랜잭션
    public void handlePaymentResultEvent(PaymentResultEvent event) {
        try {
            log.info("결제 결과 이벤트 처리 시작 - orderId: {}, paymentSeq: {}, resultType: {}", 
                    event.getOrderId(), event.getPaymentSeq(), event.getResultType());

            switch (event.getResultType()) {
                case COMPLETED -> handlePaymentCompleted(event);
                case FAILED -> handlePaymentFailed(event);
                case CANCELLED -> handlePaymentCancelled(event);
            }
            
        } catch (Exception e) {
            // 후속 처리 실패는 로그만 남기고 결제 처리에는 영향 없음
            log.error("결제 결과 이벤트 처리 실패 - orderId: {}, resultType: {}", 
                    event.getOrderId(), event.getResultType(), e);
        }
    }

    /**
     * 결제 완료 후속 처리
     */
    private void handlePaymentCompleted(PaymentResultEvent event) {
        try {
            log.info("결제 완료 후속 처리 시작 - orderId: {}, paymentSeq: {}", 
                    event.getOrderId(), event.getPaymentSeq());

            // 주문 상태를 완료로 변경
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + event.getOrderId()));
            
            order.completePayment();
            orderRepository.save(order);
            
            log.info("주문 상태 완료 처리 완료 - orderId: {}, orderStatus: {}", 
                    event.getOrderId(), order.getOrderStatus());
            
        } catch (Exception e) {
            log.error("결제 완료 후속 처리 실패 - orderId: {}", event.getOrderId(), e);
            throw e;
        }
    }

    /**
     * 결제 실패 후속 처리
     */
    private void handlePaymentFailed(PaymentResultEvent event) {
        try {
            log.info("결제 실패 후속 처리 시작 - orderId: {}, paymentSeq: {}, message: {}", 
                    event.getOrderId(), event.getPaymentSeq(), event.getMessage());

            // 결제 및 주문 정보 조회
            Payment payment = paymentRepository.findByPaymentSeq(event.getPaymentSeq())
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + event.getPaymentSeq()));
            
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + event.getOrderId()));

            // 재고/포인트 복구 (보상 트랜잭션)
            orderRecoveryService.handlePaymentFailure(payment, order);
            
            log.info("결제 실패 복구 처리 완료 - orderId: {}, paymentSeq: {}", 
                    event.getOrderId(), event.getPaymentSeq());
            
        } catch (Exception e) {
            log.error("결제 실패 후속 처리 실패 - orderId: {}", event.getOrderId(), e);
            // 복구 실패는 별도 알림이나 수동 처리 필요
        }
    }

    /**
     * 결제 취소 후속 처리
     */
    private void handlePaymentCancelled(PaymentResultEvent event) {
        try {
            log.info("결제 취소 후속 처리 시작 - orderId: {}, paymentSeq: {}", 
                    event.getOrderId(), event.getPaymentSeq());

            // 결제 및 주문 정보 조회
            Payment payment = paymentRepository.findByPaymentSeq(event.getPaymentSeq())
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + event.getPaymentSeq()));
            
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + event.getOrderId()));

            // 재고/포인트 복구 (보상 트랜잭션)
            orderRecoveryService.handlePaymentFailure(payment, order);
            
            log.info("결제 취소 복구 처리 완료 - orderId: {}, paymentSeq: {}", 
                    event.getOrderId(), event.getPaymentSeq());
            
        } catch (Exception e) {
            log.error("결제 취소 후속 처리 실패 - orderId: {}", event.getOrderId(), e);
        }
    }
}
