package com.loopers.domain.user.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 유저 행동 이벤트
 * 모든 유저의 행동을 추적하기 위한 기본 이벤트
 */
@Getter
public class UserActionEvent {
    private final String eventId;           // 이벤트 고유 ID
    private final Long userId;              // 사용자 ID (비로그인시 null)
    private final String sessionId;         // 세션 ID
    private final UserActionType actionType; // 행동 타입
    private final String targetType;        // 대상 타입 (PRODUCT, ORDER, USER 등)
    private final String targetId;          // 대상 ID
    private final Map<String, Object> properties; // 추가 속성
    private final LocalDateTime occurredAt; // 발생 시간
    private final String userAgent;         // User-Agent
    private final String ipAddress;         // IP 주소
    private final String referer;           // Referer URL

    public UserActionEvent(Long userId, String sessionId, UserActionType actionType,
                          String targetType, String targetId, Map<String, Object> properties,
                          String userAgent, String ipAddress, String referer) {
        this.eventId = generateEventId();
        this.userId = userId;
        this.sessionId = sessionId;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.properties = properties;
        this.occurredAt = LocalDateTime.now();
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.referer = referer;
    }

    private String generateEventId() {
        return "EVENT_" + System.currentTimeMillis() + "_" + Math.random();
    }

    /**
     * 상품 조회 이벤트 생성
     */
    public static UserActionEvent createProductView(Long userId, String sessionId, Long productId,
                                                   Map<String, Object> properties, String userAgent, String ipAddress, String referer) {
        return new UserActionEvent(userId, sessionId, UserActionType.PRODUCT_VIEW, "PRODUCT", 
                                  productId.toString(), properties, userAgent, ipAddress, referer);
    }

    /**
     * 상품 클릭 이벤트 생성
     */
    public static UserActionEvent createProductClick(Long userId, String sessionId, Long productId,
                                                    String clickSource, Map<String, Object> properties, String userAgent, String ipAddress, String referer) {
        properties.put("clickSource", clickSource); // 클릭 위치 (LIST, BANNER, RECOMMENDATION 등)
        return new UserActionEvent(userId, sessionId, UserActionType.PRODUCT_CLICK, "PRODUCT", 
                                  productId.toString(), properties, userAgent, ipAddress, referer);
    }

    /**
     * 상품 좋아요 이벤트 생성
     */
    public static UserActionEvent createProductLike(Long userId, String sessionId, Long productId,
                                                   boolean isLike, Map<String, Object> properties, String userAgent, String ipAddress, String referer) {
        properties.put("isLike", isLike); // true: 좋아요, false: 좋아요 취소
        return new UserActionEvent(userId, sessionId, UserActionType.PRODUCT_LIKE, "PRODUCT", 
                                  productId.toString(), properties, userAgent, ipAddress, referer);
    }

    /**
     * 주문 이벤트 생성
     */
    public static UserActionEvent createOrderAction(Long userId, String sessionId, Long orderId,
                                                   UserActionType actionType, Map<String, Object> properties, String userAgent, String ipAddress, String referer) {
        return new UserActionEvent(userId, sessionId, actionType, "ORDER", 
                                  orderId.toString(), properties, userAgent, ipAddress, referer);
    }

    /**
     * 검색 이벤트 생성
     */
    public static UserActionEvent createSearch(Long userId, String sessionId, String searchQuery,
                                             int resultCount, Map<String, Object> properties, String userAgent, String ipAddress, String referer) {
        properties.put("searchQuery", searchQuery);
        properties.put("resultCount", resultCount);
        return new UserActionEvent(userId, sessionId, UserActionType.SEARCH, "SEARCH", 
                                  searchQuery, properties, userAgent, ipAddress, referer);
    }
}
