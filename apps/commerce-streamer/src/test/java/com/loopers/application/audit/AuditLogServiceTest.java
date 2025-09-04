package com.loopers.application.audit;

import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private EventLogRepository eventLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void 기간별_이벤트_로그_조회() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = Pageable.ofSize(10);
        
        List<EventLog> eventLogs = List.of(
            createEventLog("event-1", "StockAdjusted", "1", "Product"),
            createEventLog("event-2", "LikeChanged", "1", "Product")
        );
        Page<EventLog> expectedPage = new PageImpl<>(eventLogs);

        when(eventLogRepository.findByEventOccurredAtBetween(startDate, endDate, pageable))
            .thenReturn(expectedPage);

        // when
        Page<EventLog> result = auditLogService.getEventLogsByDateRange(startDate, endDate, pageable);

        // then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void 이벤트_타입별_로그_조회() {
        // given
        String eventType = "StockAdjusted";
        List<EventLog> expectedLogs = List.of(
            createEventLog("event-1", eventType, "1", "Product"),
            createEventLog("event-2", eventType, "2", "Product")
        );

        when(eventLogRepository.findByEventTypeOrderByCreatedAtDesc(eventType))
            .thenReturn(expectedLogs);

        // when
        List<EventLog> result = auditLogService.getEventLogsByType(eventType);

        // then
        assertThat(result).isEqualTo(expectedLogs);
        assertThat(result).hasSize(2);
    }

    @Test
    void 집계_ID별_로그_조회() {
        // given
        String aggregateId = "1";
        List<EventLog> expectedLogs = List.of(
            createEventLog("event-1", "StockAdjusted", aggregateId, "Product"),
            createEventLog("event-2", "LikeChanged", aggregateId, "Product")
        );

        when(eventLogRepository.findByAggregateIdOrderByCreatedAtDesc(aggregateId))
            .thenReturn(expectedLogs);

        // when
        List<EventLog> result = auditLogService.getEventLogsByAggregateId(aggregateId);

        // then
        assertThat(result).isEqualTo(expectedLogs);
        assertThat(result).hasSize(2);
    }

    @Test
    void 감사_로그_통계_정보() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<EventLog> eventLogs = List.of(
            createEventLog("event-1", "StockAdjusted", "1", "Product"),
            createEventLog("event-2", "StockAdjusted", "2", "Product"),
            createEventLog("event-3", "LikeChanged", "1", "Product")
        );
        Page<EventLog> eventLogsPage = new PageImpl<>(eventLogs);

        when(eventLogRepository.findByEventOccurredAtBetween(eq(startDate), eq(endDate), any(Pageable.class)))
            .thenReturn(eventLogsPage);

        // when
        AuditLogStats result = auditLogService.getAuditLogStats(startDate, endDate);

        // then
        assertThat(result.getTotalEvents()).isEqualTo(3);
        assertThat(result.getEventTypeCount()).containsEntry("StockAdjusted", 2L);
        assertThat(result.getEventTypeCount()).containsEntry("LikeChanged", 1L);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
    }

    private EventLog createEventLog(String eventId, String eventType, String aggregateId, String aggregateType) {
        return EventLog.create(
            eventId,
            eventType,
            aggregateId,
            aggregateType,
            "{\"test\": \"payload\"}",
            LocalDateTime.now()
        );
    }
}
