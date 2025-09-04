package com.loopers.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 이벤트 처리 이력 관리를 위한 Repository
 */
public interface EventHandledRepository extends JpaRepository<EventHandled, Long> {

    /**
     * 특정 이벤트ID가 이미 처리되었는지 확인
     */
    boolean existsByEventId(String eventId);

    /**
     * 특정 이벤트ID와 컨슈머에 대한 처리 이력 조회
     */
    Optional<EventHandled> findByEventIdAndConsumerName(String eventId, String consumerName);

    /**
     * 특정 기간보다 오래된 처리 이력 삭제 (정리용)
     */
    @Query("DELETE FROM EventHandled e WHERE e.processedAt < :cutoffDate")
    void deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}
