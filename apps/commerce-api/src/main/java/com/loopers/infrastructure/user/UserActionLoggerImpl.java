package com.loopers.infrastructure.user;

import com.loopers.application.user.event.UserActionLogger;
import com.loopers.domain.user.event.UserActionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 사용자 행동 로거 구현체
 * 실제 환경에서는 ClickHouse, BigQuery, S3 등에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionLoggerImpl implements UserActionLogger {

    @Override
    public void save(UserActionEvent event) {
        // 실제 구현에서는:
        // 1. ClickHouse (고성능 분석용 DB)
        // 2. Amazon S3 + Athena (데이터 레이크)
        // 3. Google BigQuery (빅데이터 분석)
        // 4. Elasticsearch (로그 검색 및 분석)
        // 등에 저장
        
        log.info("ANALYTICS_DB_SAVE: eventId={}, userId={}, actionType={}, targetType={}, targetId={}, " +
                "sessionId={}, occurredAt={}, userAgent={}, ipAddress={}", 
                event.getEventId(), event.getUserId(), event.getActionType(), 
                event.getTargetType(), event.getTargetId(), event.getSessionId(), 
                event.getOccurredAt(), event.getUserAgent(), event.getIpAddress());
    }

    @Override
    public void saveBatch(List<UserActionEvent> events) {
        // 배치 저장으로 성능 최적화
        log.info("ANALYTICS_DB_BATCH_SAVE: events_count={}", events.size());
        
        for (UserActionEvent event : events) {
            save(event);
        }
    }
}
