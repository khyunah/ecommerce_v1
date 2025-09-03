package com.loopers.application.user;

import com.loopers.domain.user.event.UserActionEvent;
import com.loopers.domain.user.event.UserActionType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 유저 행동 추적 서비스
 * 비즈니스 로직과 분리하여 유저 행동을 추적
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionTracker {
    
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 상품 조회 추적
     */
    public void trackProductView(Long userId, Long productId, String category, HttpServletRequest request) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", category);
        properties.put("timestamp", System.currentTimeMillis());
        
        UserActionEvent event = UserActionEvent.createProductView(
                userId,
                getSessionId(request),
                productId,
                properties,
                getUserAgent(request),
                getClientIP(request),
                getReferer(request)
        );
        
        publishEvent(event);
    }

    /**
     * 상품 클릭 추적
     */
    public void trackProductClick(Long userId, Long productId, String clickSource, 
                                 String listType, Integer position, HttpServletRequest request) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("listType", listType);        // 상품 목록 타입 (MAIN, CATEGORY, SEARCH 등)
        properties.put("position", position);        // 목록에서의 위치
        properties.put("timestamp", System.currentTimeMillis());
        
        UserActionEvent event = UserActionEvent.createProductClick(
                userId,
                getSessionId(request),
                productId,
                clickSource,
                properties,
                getUserAgent(request),
                getClientIP(request),
                getReferer(request)
        );
        
        publishEvent(event);
    }

    /**
     * 상품 좋아요 추적
     */
    public void trackProductLike(Long userId, Long productId, boolean isLike, HttpServletRequest request) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", System.currentTimeMillis());
        
        UserActionEvent event = UserActionEvent.createProductLike(
                userId,
                getSessionId(request),
                productId,
                isLike,
                properties,
                getUserAgent(request),
                getClientIP(request),
                getReferer(request)
        );
        
        publishEvent(event);
    }

    /**
     * 주문 관련 추적
     */
    public void trackOrderAction(Long userId, Long orderId, UserActionType actionType, 
                               Map<String, Object> additionalProperties, HttpServletRequest request) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", System.currentTimeMillis());
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }
        
        UserActionEvent event = UserActionEvent.createOrderAction(
                userId,
                getSessionId(request),
                orderId,
                actionType,
                properties,
                getUserAgent(request),
                getClientIP(request),
                getReferer(request)
        );
        
        publishEvent(event);
    }

    /**
     * 검색 추적
     */
    public void trackSearch(Long userId, String searchQuery, int resultCount, 
                          String searchType, HttpServletRequest request) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("searchType", searchType);    // 검색 타입 (TEXT, FILTER, VOICE 등)
        properties.put("timestamp", System.currentTimeMillis());
        
        UserActionEvent event = UserActionEvent.createSearch(
                userId,
                getSessionId(request),
                searchQuery,
                resultCount,
                properties,
                getUserAgent(request),
                getClientIP(request),
                getReferer(request)
        );
        
        publishEvent(event);
    }

    /**
     * 일반적인 사용자 행동 추적
     */
    public void trackUserAction(Long userId, UserActionType actionType, String targetType, 
                              String targetId, Map<String, Object> properties, HttpServletRequest request) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put("timestamp", System.currentTimeMillis());
        
        UserActionEvent event = new UserActionEvent(
                userId,
                getSessionId(request),
                actionType,
                targetType,
                targetId,
                properties,
                getUserAgent(request),
                getClientIP(request),
                getReferer(request)
        );
        
        publishEvent(event);
    }

    /**
     * 이벤트 발행
     */
    private void publishEvent(UserActionEvent event) {
        try {
            log.debug("사용자 행동 이벤트 발행: userId={}, actionType={}, targetType={}, targetId={}", 
                    event.getUserId(), event.getActionType(), event.getTargetType(), event.getTargetId());
            
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // 추적 실패해도 비즈니스 로직에는 영향 없음
            log.error("사용자 행동 이벤트 발행 실패", e);
        }
    }

    // HTTP 요청에서 정보 추출하는 유틸리티 메서드들
    private String getSessionId(HttpServletRequest request) {
        return request.getSession().getId();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }

    private String getReferer(HttpServletRequest request) {
        return request.getHeader("Referer");
    }
}
