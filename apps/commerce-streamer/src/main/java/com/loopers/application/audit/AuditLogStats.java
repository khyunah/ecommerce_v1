package com.loopers.application.audit;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 감사 로그 통계 정보
 */
@Getter
@Builder
public class AuditLogStats {
    
    private final long totalEvents;
    private final Map<String, Long> eventTypeCount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
}
