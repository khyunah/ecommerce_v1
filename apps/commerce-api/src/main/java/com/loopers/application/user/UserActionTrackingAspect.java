package com.loopers.application.user;

import com.loopers.domain.user.event.UserActionEvent;
import com.loopers.domain.user.event.UserActionType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 행동 추적 AOP
 * @TrackUserAction 어노테이션이 있는 메서드를 자동으로 추적
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserActionTrackingAspect {
    
    private final ApplicationEventPublisher eventPublisher;

    @AfterReturning("@annotation(trackUserAction)")
    public void trackUserAction(JoinPoint joinPoint, TrackUserAction trackUserAction) {
        try {
            // HTTP 요청 정보 가져오기
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            // 메서드 파라미터와 값 추출
            Method method = getMethod(joinPoint);
            Object[] args = joinPoint.getArgs();
            Parameter[] parameters = method.getParameters();

            // 사용자 ID 추출
            Long userId = extractUserId(trackUserAction.userIdParam(), parameters, args, request);
            
            // 대상 ID 추출
            String targetId = extractTargetId(trackUserAction.targetIdParam(), parameters, args);
            
            // 추가 속성 추출
            Map<String, Object> properties = extractProperties(trackUserAction.propertyParams(), parameters, args);
            properties.put("timestamp", System.currentTimeMillis());
            properties.put("method", method.getName());
            properties.put("className", joinPoint.getTarget().getClass().getSimpleName());

            // 이벤트 생성 및 발행
            UserActionEvent event = new UserActionEvent(
                    userId,
                    getSessionId(request),
                    trackUserAction.actionType(),
                    trackUserAction.targetType(),
                    targetId,
                    properties,
                    getUserAgent(request),
                    getClientIP(request),
                    getReferer(request)
            );

            eventPublisher.publishEvent(event);
            
            log.debug("사용자 행동 자동 추적 완료 - userId: {}, actionType: {}, targetType: {}", 
                    userId, trackUserAction.actionType(), trackUserAction.targetType());

        } catch (Exception e) {
            log.error("사용자 행동 자동 추적 실패", e);
        }
    }

    private Method getMethod(JoinPoint joinPoint) throws NoSuchMethodException {
        String methodName = joinPoint.getSignature().getName();
        Class<?>[] parameterTypes = new Class<?>[joinPoint.getArgs().length];
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        
        return joinPoint.getTarget().getClass().getMethod(methodName, parameterTypes);
    }

    private Long extractUserId(String userIdParam, Parameter[] parameters, Object[] args, HttpServletRequest request) {
        // 1. 파라미터에서 사용자 ID 찾기
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(userIdParam) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        
        // 2. 헤더에서 사용자 ID 찾기
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("Invalid User-Id header: {}", userIdHeader);
            }
        }
        
        return null;
    }

    private String extractTargetId(String targetIdParam, Parameter[] parameters, Object[] args) {
        if (targetIdParam.isEmpty()) {
            return null;
        }
        
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(targetIdParam)) {
                return args[i] != null ? args[i].toString() : null;
            }
        }
        
        return null;
    }

    private Map<String, Object> extractProperties(String[] propertyParams, Parameter[] parameters, Object[] args) {
        Map<String, Object> properties = new HashMap<>();
        
        for (String propertyParam : propertyParams) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(propertyParam)) {
                    properties.put(propertyParam, args[i]);
                    break;
                }
            }
        }
        
        return properties;
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
