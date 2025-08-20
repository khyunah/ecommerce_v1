package com.loopers.application.payment;

import com.loopers.application.order.OrderRecoveryService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.PgCallbackRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentCallbackService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentStatusService paymentStatusService;
    private final OrderRecoveryService orderRecoveryService;

    /**
     * PG 콜백 처리
     */
    @Transactional
    public void processCallback(PgCallbackRequest request) {
        System.out.println("콜백 처리 시작 - orderId: " + request.orderId());
        
        // 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentSeq(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + request.orderId()));
        
        // 이미 처리된 콜백인지 확인 (중복 처리 방지)
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || 
            payment.getPaymentStatus() == PaymentStatus.CANCELED) {
            System.out.println("이미 처리된 결제입니다 - paymentSeq: " + payment.getPaymentSeq() + 
                             ", status: " + payment.getPaymentStatus());
            return;
        }
        
        // 주문 정보 조회
        Order order = orderRepository.findById(payment.getRefOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + payment.getRefOrderId()));
        
        // 콜백 상태에 따른 처리
        switch (request.status()) {
            case "COMPLETED" -> {
                // 결제 완료 처리
                payment.completePayment(request.transactionKey());
                order.completePayment();
                
                paymentRepository.save(payment);
                orderRepository.save(order);
                
                System.out.println("콜백으로 결제 완료 처리 - paymentSeq: " + payment.getPaymentSeq());
            }
            case "FAILED" -> {
                // 결제 실패 처리
                payment.failPayment(request.message());
                
                paymentRepository.save(payment);
                
                // 재고/포인트 복구
                orderRecoveryService.handlePaymentFailure(payment, order);
                
                System.out.println("콜백으로 결제 실패 처리 - paymentSeq: " + payment.getPaymentSeq());
            }
            case "CANCELED" -> {
                // 결제 취소 처리
                payment.cancelPayment(request.message());
                
                paymentRepository.save(payment);
                
                // 재고/포인트 복구
                orderRecoveryService.handlePaymentFailure(payment, order);
                
                System.out.println("콜백으로 결제 취소 처리 - paymentSeq: " + payment.getPaymentSeq());
            }
            default -> {
                System.out.println("알 수 없는 콜백 상태 - paymentSeq: " + payment.getPaymentSeq() + 
                                 ", status: " + request.status());
            }
        }
    }

    /**
     * 수동 결제 상태 확인
     */
    @Transactional
    public String manualStatusCheck(String paymentSeq) {
        System.out.println("수동 결제 상태 확인 - paymentSeq: " + paymentSeq);
        
        // 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentSeq(paymentSeq)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentSeq));
        
        // 현재 상태가 완료/취소인 경우 확인 불필요
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || 
            payment.getPaymentStatus() == PaymentStatus.CANCELED || 
            payment.getPaymentStatus() == PaymentStatus.FAILED) {
            return "이미 처리 완료된 결제입니다. 상태: " + payment.getPaymentStatus();
        }
        
        try {
            // PG사에서 상태 확인
            PgPaymentResponse pgResponse = paymentStatusService.getPaymentStatus(paymentSeq);
            
            if (pgResponse.isSuccess() && pgResponse.data() != null) {
                // 이전 상태 저장
                PaymentStatus previousStatus = payment.getPaymentStatus();
                
                // 상태 동기화
                paymentStatusService.syncPaymentStatus(payment, pgResponse);
                
                // 주문 상태도 업데이트 필요한 경우 처리
                if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    Order order = orderRepository.findById(payment.getRefOrderId())
                            .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다"));
                    order.completePayment();
                    orderRepository.save(order);
                }
                // 결제 실패/취소인 경우 복구 처리
                else if (payment.getPaymentStatus() == PaymentStatus.FAILED || 
                        payment.getPaymentStatus() == PaymentStatus.CANCELED) {
                    Order order = orderRepository.findById(payment.getRefOrderId())
                            .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다"));
                    orderRecoveryService.handlePaymentFailure(payment, order);
                }
                
                return String.format("상태 확인 완료. %s → %s", 
                                   previousStatus, payment.getPaymentStatus());
                
            } else {
                return "PG사 상태 확인 실패: " + pgResponse.meta().message();
            }
            
        } catch (Exception e) {
            System.out.println("수동 상태 확인 중 오류: " + e.getMessage());
            return "상태 확인 중 오류 발생: " + e.getMessage();
        }
    }
}
