package com.loopers.application.user.event;

import com.loopers.domain.user.event.UserActionEvent;

/**
 * 사용자 행동 로거 인터페이스
 * 다양한 저장소에 사용자 행동을 저장
 */
public interface UserActionLogger {
    
    /**
     * 사용자 행동 저장
     */
    void save(UserActionEvent event);
    
    /**
     * 배치로 사용자 행동 저장 (성능 최적화)
     */
    void saveBatch(java.util.List<UserActionEvent> events);
}
