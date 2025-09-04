package com.loopers.interfaces.api;

import com.loopers.application.audit.AuditLogService;
import com.loopers.application.audit.AuditLogStats;
import com.loopers.domain.event.EventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 조회 API
 * (선택적 구현 - 과제 필수 아님)
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 기간별 이벤트 로그 조회
     */
    @GetMapping
    public ResponseEntity<Page<EventLog>> getEventLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        Page<EventLog> eventLogs = auditLogService.getEventLogsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(eventLogs);
    }

    /**
     * 이벤트 타입별 로그 조회
     */
    @GetMapping("/by-type/{eventType}")
    public ResponseEntity<List<EventLog>> getEventLogsByType(@PathVariable String eventType) {
        List<EventLog> eventLogs = auditLogService.getEventLogsByType(eventType);
        return ResponseEntity.ok(eventLogs);
    }

    /**
     * 집계 ID별 로그 조회 (상품 또는 주문 추적)
     */
    @GetMapping("/by-aggregate/{aggregateId}")
    public ResponseEntity<List<EventLog>> getEventLogsByAggregateId(@PathVariable String aggregateId) {
        List<EventLog> eventLogs = auditLogService.getEventLogsByAggregateId(aggregateId);
        return ResponseEntity.ok(eventLogs);
    }

    /**
     * 집계 타입과 ID로 로그 조회
     */
    @GetMapping("/by-aggregate/{aggregateType}/{aggregateId}")
    public ResponseEntity<List<EventLog>> getEventLogsByAggregateTypeAndId(
            @PathVariable String aggregateType,
            @PathVariable String aggregateId) {
        
        List<EventLog> eventLogs = auditLogService.getEventLogsByAggregateTypeAndId(aggregateType, aggregateId);
        return ResponseEntity.ok(eventLogs);
    }

    /**
     * 감사 로그 통계 정보
     */
    @GetMapping("/stats")
    public ResponseEntity<AuditLogStats> getAuditLogStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        AuditLogStats stats = auditLogService.getAuditLogStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
