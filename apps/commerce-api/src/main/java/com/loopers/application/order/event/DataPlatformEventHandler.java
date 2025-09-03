package com.loopers.application.order.event;

import com.loopers.domain.order.ExternalOrderSender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.event.OrderCompletedEvent;
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
public class DataPlatformEventHandler {
    
    private final ExternalOrderSender externalOrderSender;
    private final OrderRepository orderRepository;

    /**
     * 주문 완료 시 데이터 플랫폼 전송
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCompletedEvent(OrderCompletedEvent event) {
        try {
            log.info("주문 완료 데이터 플랫폼 전송 시작 - orderId: {}, userId: {}", 
                    event.getOrderId(), event.getUserId());

            // 주문 정보 조회
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + event.getOrderId()));
            
            // 데이터 플랫폼으로 전송
            externalOrderSender.sendOrder(order);
            
            log.info("주문 완료 데이터 플랫폼 전송 완료 - orderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            // 전송 실패해도 주문 처리에는 영향 없음
            log.error("주문 완료 데이터 플랫폼 전송 실패 - orderId: {}", event.getOrderId(), e);
            // 필요시 재시도 큐에 추가하거나 알림 발송
        }
    }

    /**
     * 결제 완료 시 데이터 플랫폼 전송 (결제 완료 이벤트 수신)
     * 결제 완료 후에도 데이터 플랫폼에 전송해야 하는 경우
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentCompletedEvent(PaymentResultEvent event) {
        try {
            // 결제 완료인 경우만 처리
            if (event.getResultType() != PaymentResultEvent.PaymentResultType.COMPLETED) {
                return;
            }

            log.info("결제 완료 데이터 플랫폼 전송 시작 - orderId: {}, paymentSeq: {}", 
                    event.getOrderId(), event.getPaymentSeq());

            // 주문 정보 조회
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + event.getOrderId()));
            
            // 결제 완료된 주문만 데이터 플랫폼으로 전송
            if (order.isPaymentCompleted()) {
                externalOrderSender.sendOrder(order);
                log.info("결제 완료 데이터 플랫폼 전송 완료 - orderId: {}", event.getOrderId());
            } else {
                log.warn("결제 미완료 주문 - 데이터 플랫폼 전송 생략 - orderId: {}", event.getOrderId());
            }
            
        } catch (Exception e) {
            // 전송 실패해도 결제 처리에는 영향 없음
            log.error("결제 완료 데이터 플랫폼 전송 실패 - orderId: {}", event.getOrderId(), e);
            // 필요시 재시도 큐에 추가하거나 알림 발송
        }
    }

    /**
     * 결제 실패/취소 시 데이터 플랫폼에 취소 정보 전송
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentFailedEvent(PaymentResultEvent event) {
        try {
            // 결제 실패/취소인 경우만 처리
            if (event.getResultType() == PaymentResultEvent.PaymentResultType.COMPLETED) {
                return;
            }

            log.info("결제 실패/취소 데이터 플랫폼 전송 시작 - orderId: {}, resultType: {}", 
                    event.getOrderId(), event.getResultType());

            // 주문 정보 조회
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + event.getOrderId()));
            
            // 결제 실패/취소 정보를 데이터 플랫폼으로 전송
            externalOrderSender.sendOrderCancellation(order, event.getResultType().name(), event.getMessage());
            
            log.info("결제 실패/취소 데이터 플랫폼 전송 완료 - orderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            // 전송 실패해도 결제 처리에는 영향 없음
            log.error("결제 실패/취소 데이터 플랫폼 전송 실패 - orderId: {}", event.getOrderId(), e);
        }
    }
}
