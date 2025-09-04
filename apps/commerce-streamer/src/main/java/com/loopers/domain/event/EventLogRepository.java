package com.loopers.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 로그 관리를 위한 Repository
 */
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    /**
     * 특정 이벤트 타입으로 이벤트 로그 조회
     */
    List<EventLog> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * 특정 집계 ID로 이벤트 로그 조회
     */
    List<EventLog> findByAggregateIdOrderByCreatedAtDesc(String aggregateId);

    /**
     * 특정 기간 내의 이벤트 로그 조회
     */
    @Query("SELECT e FROM EventLog e WHERE e.eventOccurredAt BETWEEN :startDate AND :endDate ORDER BY e.eventOccurredAt DESC")
    Page<EventLog> findByEventOccurredAtBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate, 
                                               Pageable pageable);

    /**
     * 특정 집계 타입과 ID로 이벤트 로그 조회
     */
    List<EventLog> findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(String aggregateType, String aggregateId);

    /**
     * 특정 기간보다 오래된 이벤트 로그 삭제 (정리용)
     */
    @Query("DELETE FROM EventLog e WHERE e.eventOccurredAt < :cutoffDate")
    void deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}
