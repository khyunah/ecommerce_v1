package com.loopers.application.user.event;

import com.loopers.domain.user.event.UserActionEvent;

/**
 * 사용자 행동 분석기 인터페이스
 * 실시간 분석 및 외부 시스템 연동
 */
public interface UserActionAnalyzer {
    
    /**
     * 외부 분석 시스템으로 전송
     */
    void sendToExternalSystems(UserActionEvent event);
    
    /**
     * 실시간 분석 처리
     */
    void processRealTime(UserActionEvent event);
}
