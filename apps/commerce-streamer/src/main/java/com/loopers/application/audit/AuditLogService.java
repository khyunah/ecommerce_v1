package com.loopers.application.audit;

import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 조회 서비스
 * event_log 테이블에서 감사 로그를 조회하는 기능 제공
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuditLogService {

    private final EventLogRepository eventLogRepository;

    /**
     * 특정 기간의 이벤트 로그 조회
     */
    public Page<EventLog> getEventLogsByDateRange(LocalDateTime startDate, 
                                                 LocalDateTime endDate, 
                                                 Pageable pageable) {
        return eventLogRepository.findByEventOccurredAtBetween(startDate, endDate, pageable);
    }

    /**
     * 특정 이벤트 타입의 로그 조회
     */
    public List<EventLog> getEventLogsByType(String eventType) {
        return eventLogRepository.findByEventTypeOrderByCreatedAtDesc(eventType);
    }

    /**
     * 특정 집계 ID의 이벤트 로그 조회 (상품 또는 주문 추적)
     */
    public List<EventLog> getEventLogsByAggregateId(String aggregateId) {
        return eventLogRepository.findByAggregateIdOrderByCreatedAtDesc(aggregateId);
    }

    /**
     * 특정 집계 타입과 ID의 이벤트 로그 조회
     */
    public List<EventLog> getEventLogsByAggregateTypeAndId(String aggregateType, String aggregateId) {
        return eventLogRepository.findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(
            aggregateType, aggregateId);
    }

    /**
     * 감사 로그 통계 정보
     */
    public AuditLogStats getAuditLogStats(LocalDateTime startDate, LocalDateTime endDate) {
        Page<EventLog> logs = eventLogRepository.findByEventOccurredAtBetween(
            startDate, endDate, Pageable.unpaged());
        
        long totalEvents = logs.getTotalElements();
        
        // 이벤트 타입별 개수 계산
        var eventTypeCount = logs.getContent().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                EventLog::getEventType,
                java.util.stream.Collectors.counting()
            ));
            
        return AuditLogStats.builder()
            .totalEvents(totalEvents)
            .eventTypeCount(eventTypeCount)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }
}
