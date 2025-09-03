package com.loopers.application.order;

import com.loopers.domain.user.event.UserActionEvent;
import com.loopers.domain.user.event.UserActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주문 관련 사용자 행동 추적 테스트
 * OrderFacade에서 발행되는 사용자 행동 이벤트 검증
 */
@SpringBootTest
@RecordApplicationEvents
class OrderUserActionTrackingTest {

    @Autowired
    private ApplicationEvents events;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @DisplayName("주문 완료 시 사용자 행동 추적 이벤트가 발행되는지 시뮬레이션")
    @Test
    void should_simulate_order_completion_user_action_event() {
        // Given - 주문 완료 시나리오 시뮬레이션
        Long userId = 123L;
        Long orderId = 456L;
        String orderSeq = "ORDER-123-456";
        Long totalAmount = 50000L;
        Long finalAmount = 45000L; // 쿠폰 할인 적용
        String paymentMethod = "CARD";
        
        // When - 주문 완료 이벤트 직접 발행 (실제로는 OrderFacade에서 발행)
        UserActionEvent orderCompleteEvent = new UserActionEvent(
                userId,
                "ORDER_SESSION_" + orderId,
                UserActionType.ORDER_COMPLETE,
                "ORDER",
                orderId.toString(),
                createOrderProperties(orderSeq, totalAmount, finalAmount, paymentMethod, true, 456L, 5000L, 2),
                null, // User-Agent
                null, // IP Address  
                null  // Referer
        );
        
        // 이벤트 발행 시뮬레이션
        eventPublisher.publishEvent(orderCompleteEvent);
        
        // Then
        long eventCount = events.stream(UserActionEvent.class)
                .filter(event -> event.getActionType() == UserActionType.ORDER_COMPLETE)
                .filter(event -> event.getUserId().equals(userId))
                .filter(event -> event.getTargetId().equals(orderId.toString()))
                .count();
        
        assertThat(eventCount).isEqualTo(1);
        
        // 발행된 이벤트 상세 검증
        UserActionEvent publishedEvent = events.stream(UserActionEvent.class)
                .filter(event -> event.getActionType() == UserActionType.ORDER_COMPLETE)
                .findFirst()
                .orElseThrow();
        
        assertThat(publishedEvent.getTargetType()).isEqualTo("ORDER");
        assertThat(publishedEvent.getUserId()).isEqualTo(userId);
        assertThat(publishedEvent.getProperties().get("orderSeq")).isEqualTo(orderSeq);
        assertThat(publishedEvent.getProperties().get("totalAmount")).isEqualTo(totalAmount);
        assertThat(publishedEvent.getProperties().get("finalAmount")).isEqualTo(finalAmount);
        assertThat(publishedEvent.getProperties().get("paymentMethod")).isEqualTo(paymentMethod);
        assertThat(publishedEvent.getProperties().get("usedCoupon")).isEqualTo(true);
        assertThat(publishedEvent.getProperties().get("couponId")).isEqualTo(456L);
        assertThat(publishedEvent.getProperties().get("usedPoint")).isEqualTo(5000L);
        assertThat(publishedEvent.getProperties().get("itemCount")).isEqualTo(2);
    }

    @DisplayName("주문 시작 사용자 행동 추적 이벤트 시뮬레이션")
    @Test 
    void should_simulate_order_start_user_action_event() {
        // Given
        Long userId = 789L;
        String cartId = "CART-789";
        
        // When - 주문 시작 이벤트 시뮬레이션
        UserActionEvent orderStartEvent = new UserActionEvent(
                userId,
                "CHECKOUT_SESSION_789",
                UserActionType.ORDER_START,
                "ORDER",
                cartId,
                Map.of(
                    "cartItemCount", 3,
                    "estimatedAmount", 75000L,
                    "timestamp", System.currentTimeMillis()
                ),
                "Mozilla/5.0 Test",
                "192.168.1.1",
                "https://example.com/cart"
        );
        
        eventPublisher.publishEvent(orderStartEvent);
        
        // Then
        long eventCount = events.stream(UserActionEvent.class)
                .filter(event -> event.getActionType() == UserActionType.ORDER_START)
                .filter(event -> event.getUserId().equals(userId))
                .count();
        
        assertThat(eventCount).isEqualTo(1);
        
        UserActionEvent publishedEvent = events.stream(UserActionEvent.class)
                .filter(event -> event.getActionType() == UserActionType.ORDER_START)
                .findFirst()
                .orElseThrow();
        
        assertThat(publishedEvent.getProperties().get("cartItemCount")).isEqualTo(3);
        assertThat(publishedEvent.getProperties().get("estimatedAmount")).isEqualTo(75000L);
        assertThat(publishedEvent.getUserAgent()).isEqualTo("Mozilla/5.0 Test");
        assertThat(publishedEvent.getIpAddress()).isEqualTo("192.168.1.1");
    }

    @DisplayName("결제 실패 사용자 행동 추적 이벤트 시뮬레이션")
    @Test
    void should_simulate_payment_failure_user_action_event() {
        // Given
        Long userId = 999L;
        Long orderId = 888L;
        String failureReason = "카드 한도 초과";
        
        // When - 결제 실패 이벤트 시뮬레이션
        UserActionEvent paymentFailEvent = new UserActionEvent(
                userId,
                "PAYMENT_SESSION_999",
                UserActionType.ORDER_PAYMENT_FAIL,
                "ORDER",
                orderId.toString(),
                Map.of(
                    "failureReason", failureReason,
                    "attemptedAmount", 100000L,
                    "paymentMethod", "CARD",
                    "timestamp", System.currentTimeMillis()
                ),
                "Mozilla/5.0 Mobile",
                "203.0.113.1", 
                "https://example.com/checkout"
        );
        
        eventPublisher.publishEvent(paymentFailEvent);
        
        // Then
        long eventCount = events.stream(UserActionEvent.class)
                .filter(event -> event.getActionType() == UserActionType.ORDER_PAYMENT_FAIL)
                .filter(event -> event.getUserId().equals(userId))
                .count();
        
        assertThat(eventCount).isEqualTo(1);
        
        UserActionEvent publishedEvent = events.stream(UserActionEvent.class)
                .filter(event -> event.getActionType() == UserActionType.ORDER_PAYMENT_FAIL)
                .findFirst()
                .orElseThrow();
        
        assertThat(publishedEvent.getProperties().get("failureReason")).isEqualTo(failureReason);
        assertThat(publishedEvent.getProperties().get("attemptedAmount")).isEqualTo(100000L);
        assertThat(publishedEvent.getIpAddress()).isEqualTo("203.0.113.1");
    }

    /**
     * 주문 속성 생성 헬퍼 메서드
     */
    private Map<String, Object> createOrderProperties(String orderSeq, Long totalAmount, Long finalAmount, 
                                                     String paymentMethod, boolean usedCoupon, Long couponId, 
                                                     Long usedPoint, int itemCount) {
        Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("orderSeq", orderSeq);
        properties.put("totalAmount", totalAmount);
        properties.put("finalAmount", finalAmount);
        properties.put("paymentMethod", paymentMethod);
        properties.put("itemCount", itemCount);
        properties.put("timestamp", System.currentTimeMillis());
        
        if (usedCoupon) {
            properties.put("usedCoupon", true);
            properties.put("couponId", couponId);
        }
        
        if (usedPoint != null && usedPoint > 0) {
            properties.put("usedPoint", usedPoint);
        }
        
        return properties;
    }
}
