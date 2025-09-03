package com.loopers.application.user;

import com.loopers.domain.user.event.UserActionEvent;
import com.loopers.domain.user.event.UserActionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserActionTrackerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpSession session;
    
    @InjectMocks
    private UserActionTracker userActionTracker;

    @DisplayName("상품 조회 추적 시 올바른 이벤트가 발행된다")
    @Test
    void should_publish_product_view_event_when_tracking_product_view() {
        // Given
        Long userId = 123L;
        Long productId = 456L;
        String category = "ELECTRONICS";
        
        mockHttpRequest();
        
        // When
        userActionTracker.trackProductView(userId, productId, category, request);
        
        // Then
        ArgumentCaptor<UserActionEvent> eventCaptor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserActionEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isEqualTo(userId);
        assertThat(publishedEvent.getActionType()).isEqualTo(UserActionType.PRODUCT_VIEW);
        assertThat(publishedEvent.getTargetType()).isEqualTo("PRODUCT");
        assertThat(publishedEvent.getTargetId()).isEqualTo(productId.toString());
        assertThat(publishedEvent.getSessionId()).isEqualTo("SESSION_123");
        assertThat(publishedEvent.getProperties().get("category")).isEqualTo(category);
        assertThat(publishedEvent.getUserAgent()).isEqualTo("Mozilla/5.0 Test");
        assertThat(publishedEvent.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(publishedEvent.getReferer()).isEqualTo("https://example.com");
    }

    @DisplayName("상품 클릭 추적 시 클릭 정보가 포함된 이벤트가 발행된다")
    @Test
    void should_publish_product_click_event_with_click_info_when_tracking_product_click() {
        // Given
        Long userId = 123L;
        Long productId = 456L;
        String clickSource = "BANNER";
        String listType = "MAIN_PAGE";
        Integer position = 3;
        
        mockHttpRequest();
        
        // When
        userActionTracker.trackProductClick(userId, productId, clickSource, listType, position, request);
        
        // Then
        ArgumentCaptor<UserActionEvent> eventCaptor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserActionEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isEqualTo(userId);
        assertThat(publishedEvent.getActionType()).isEqualTo(UserActionType.PRODUCT_CLICK);
        assertThat(publishedEvent.getTargetType()).isEqualTo("PRODUCT");
        assertThat(publishedEvent.getTargetId()).isEqualTo(productId.toString());
        assertThat(publishedEvent.getProperties().get("clickSource")).isEqualTo(clickSource);
        assertThat(publishedEvent.getProperties().get("listType")).isEqualTo(listType);
        assertThat(publishedEvent.getProperties().get("position")).isEqualTo(position);
    }

    @DisplayName("상품 좋아요 추적 시 좋아요 정보가 포함된 이벤트가 발행된다")
    @Test
    void should_publish_product_like_event_with_like_info_when_tracking_product_like() {
        // Given
        Long userId = 123L;
        Long productId = 456L;
        boolean isLike = true;
        
        mockHttpRequest();
        
        // When
        userActionTracker.trackProductLike(userId, productId, isLike, request);
        
        // Then
        ArgumentCaptor<UserActionEvent> eventCaptor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserActionEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isEqualTo(userId);
        assertThat(publishedEvent.getActionType()).isEqualTo(UserActionType.PRODUCT_LIKE);
        assertThat(publishedEvent.getTargetType()).isEqualTo("PRODUCT");
        assertThat(publishedEvent.getTargetId()).isEqualTo(productId.toString());
        assertThat(publishedEvent.getProperties().get("isLike")).isEqualTo(isLike);
    }

    @DisplayName("주문 행동 추적 시 주문 정보가 포함된 이벤트가 발행된다")
    @Test
    void should_publish_order_action_event_when_tracking_order_action() {
        // Given
        Long userId = 123L;
        Long orderId = 789L;
        UserActionType actionType = UserActionType.ORDER_COMPLETE;
        Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put("totalAmount", 50000L);
        additionalProperties.put("paymentMethod", "CARD");
        
        mockHttpRequest();
        
        // When
        userActionTracker.trackOrderAction(userId, orderId, actionType, additionalProperties, request);
        
        // Then
        ArgumentCaptor<UserActionEvent> eventCaptor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserActionEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isEqualTo(userId);
        assertThat(publishedEvent.getActionType()).isEqualTo(actionType);
        assertThat(publishedEvent.getTargetType()).isEqualTo("ORDER");
        assertThat(publishedEvent.getTargetId()).isEqualTo(orderId.toString());
        assertThat(publishedEvent.getProperties().get("totalAmount")).isEqualTo(50000L);
        assertThat(publishedEvent.getProperties().get("paymentMethod")).isEqualTo("CARD");
    }

    @DisplayName("검색 추적 시 검색 정보가 포함된 이벤트가 발행된다")
    @Test
    void should_publish_search_event_with_search_info_when_tracking_search() {
        // Given
        Long userId = 123L;
        String searchQuery = "스마트폰";
        int resultCount = 15;
        String searchType = "TEXT";
        
        mockHttpRequest();
        
        // When
        userActionTracker.trackSearch(userId, searchQuery, resultCount, searchType, request);
        
        // Then
        ArgumentCaptor<UserActionEvent> eventCaptor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserActionEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isEqualTo(userId);
        assertThat(publishedEvent.getActionType()).isEqualTo(UserActionType.SEARCH);
        assertThat(publishedEvent.getTargetType()).isEqualTo("SEARCH");
        assertThat(publishedEvent.getTargetId()).isEqualTo(searchQuery);
        assertThat(publishedEvent.getProperties().get("searchQuery")).isEqualTo(searchQuery);
        assertThat(publishedEvent.getProperties().get("resultCount")).isEqualTo(resultCount);
        assertThat(publishedEvent.getProperties().get("searchType")).isEqualTo(searchType);
    }

    @DisplayName("사용자 ID가 null이어도 이벤트가 발행된다")
    @Test
    void should_publish_event_even_when_user_id_is_null() {
        // Given
        Long userId = null; // 비로그인 사용자
        Long productId = 456L;
        String category = "ELECTRONICS";
        
        mockHttpRequest();
        
        // When
        userActionTracker.trackProductView(userId, productId, category, request);
        
        // Then
        ArgumentCaptor<UserActionEvent> eventCaptor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserActionEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isNull();
        assertThat(publishedEvent.getActionType()).isEqualTo(UserActionType.PRODUCT_VIEW);
        assertThat(publishedEvent.getSessionId()).isEqualTo("SESSION_123");
    }

    @DisplayName("이벤트 발행 중 예외가 발생해도 메서드가 정상 완료된다")
    @Test
    void should_complete_normally_even_when_event_publishing_fails() {
        // Given
        Long userId = 123L;
        Long productId = 456L;
        String category = "ELECTRONICS";
        
        mockHttpRequest();
        
        // 이벤트 발행 시 예외 발생하도록 설정 (void 메서드는 doThrow 사용)
        doThrow(new RuntimeException("Event publishing failed")).when(eventPublisher).publishEvent(any());
        
        // When & Then - 예외가 발생하지 않고 메서드가 정상 완료되어야 함
        userActionTracker.trackProductView(userId, productId, category, request);
        
        verify(eventPublisher).publishEvent(any(UserActionEvent.class));
    }

    private void mockHttpRequest() {
        given(request.getSession()).willReturn(session);
        given(session.getId()).willReturn("SESSION_123");
        given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0 Test");
        given(request.getHeader("X-Forwarded-For")).willReturn(null);
        given(request.getHeader("X-Real-IP")).willReturn(null);
        given(request.getRemoteAddr()).willReturn("192.168.1.1");
        given(request.getHeader("Referer")).willReturn("https://example.com");
    }
}
