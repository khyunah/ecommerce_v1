package com.loopers.application.user.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.user.event.UserActionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저 행동 이벤트 핸들러
 * - 비동기로 로깅 및 분석 데이터 처리
 * - 메인 비즈니스 로직과 완전 분리
 * - 실패해도 비즈니스 로직에 영향 없음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionEventHandler {
    
    private final ObjectMapper objectMapper;
    private final UserActionLogger userActionLogger;
    private final UserActionAnalyzer userActionAnalyzer;

    /**
     * 유저 행동 이벤트 처리
     * - 구조화된 로깅
     * - 분석 데이터 적재
     * - 외부 분석 시스템 전송
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserActionEvent(UserActionEvent event) {
        try {
            log.info("사용자 행동 이벤트 처리 시작 - eventId: {}, userId: {}, actionType: {}", 
                    event.getEventId(), event.getUserId(), event.getActionType());

            // 1. 구조화된 로깅 (JSON 형태로 로깅)
            logUserAction(event);
            
            // 2. 분석용 데이터베이스 저장
            saveToAnalyticsDatabase(event);
            
            // 3. 외부 분석 시스템으로 전송 (Google Analytics, Adobe Analytics 등)
            sendToExternalAnalytics(event);
            
            // 4. 실시간 분석 처리 (Redis Stream, Kafka 등)
            processRealTimeAnalytics(event);
            
            log.debug("사용자 행동 이벤트 처리 완료 - eventId: {}", event.getEventId());
            
        } catch (Exception e) {
            // 추적 실패해도 비즈니스 로직에는 영향 없음
            log.error("사용자 행동 이벤트 처리 실패 - eventId: {}, userId: {}", 
                    event.getEventId(), event.getUserId(), e);
        }
    }

    /**
     * 구조화된 로깅
     */
    private void logUserAction(UserActionEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            // 구조화된 로그 출력 (ELK Stack에서 파싱하기 쉬움)
            log.info("USER_ACTION_EVENT: {}", eventJson);
            
        } catch (JsonProcessingException e) {
            log.error("사용자 행동 이벤트 JSON 변환 실패 - eventId: {}", event.getEventId(), e);
        }
    }

    /**
     * 분석용 데이터베이스 저장
     */
    private void saveToAnalyticsDatabase(UserActionEvent event) {
        try {
            userActionLogger.save(event);
            log.debug("사용자 행동 데이터 저장 완료 - eventId: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("사용자 행동 데이터 저장 실패 - eventId: {}", event.getEventId(), e);
        }
    }

    /**
     * 외부 분석 시스템 전송
     */
    private void sendToExternalAnalytics(UserActionEvent event) {
        try {
            // Google Analytics 4, Adobe Analytics, Mixpanel 등으로 전송
            userActionAnalyzer.sendToExternalSystems(event);
            log.debug("외부 분석 시스템 전송 완료 - eventId: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("외부 분석 시스템 전송 실패 - eventId: {}", event.getEventId(), e);
        }
    }

    /**
     * 실시간 분석 처리
     */
    private void processRealTimeAnalytics(UserActionEvent event) {
        try {
            // Redis Stream, Apache Kafka 등을 통한 실시간 처리
            userActionAnalyzer.processRealTime(event);
            log.debug("실시간 분석 처리 완료 - eventId: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("실시간 분석 처리 실패 - eventId: {}", event.getEventId(), e);
        }
    }
}
