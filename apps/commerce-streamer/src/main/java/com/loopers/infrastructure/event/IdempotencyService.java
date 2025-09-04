package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이벤트 멱등성 처리 서비스
 * event_handled 테이블을 활용한 중복 처리 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final EventHandledRepository eventHandledRepository;

    /**
     * 이벤트가 이미 처리되었는지 확인
     */
    public boolean isEventAlreadyHandled(String eventId, String consumerName) {
        boolean isHandled = eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName)
                .isPresent();
        
        if (isHandled) {
            log.debug("Event already handled - EventId: {}, Consumer: {}", eventId, consumerName);
        }
        
        return isHandled;
    }

    /**
     * 이벤트 처리 완료를 기록
     * 트랜잭션 내에서 호출되어야 함
     */
    @Transactional
    public void markEventAsHandled(String eventId, String eventType, String consumerName) {
        try {
            // 중복 체크 (race condition 방지)
            if (eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName).isPresent()) {
                log.debug("Event already marked as handled - EventId: {}, Consumer: {}", eventId, consumerName);
                return;
            }

            EventHandled eventHandled = EventHandled.create(eventId, eventType, consumerName);
            eventHandledRepository.save(eventHandled);
            
            log.debug("Marked event as handled - EventId: {}, EventType: {}, Consumer: {}", 
                    eventId, eventType, consumerName);
                    
        } catch (Exception e) {
            // 동시성으로 인한 중복 삽입 시도는 무시 (이미 처리된 것으로 간주)
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                log.debug("Event already handled by another thread - EventId: {}, Consumer: {}", 
                        eventId, consumerName);
            } else {
                log.error("Failed to mark event as handled - EventId: {}, Consumer: {}, Error: {}", 
                        eventId, consumerName, e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * 이벤트 처리 전 멱등성 확인 및 처리 후 기록을 원자적으로 수행
     */
    @Transactional
    public boolean processWithIdempotency(String eventId, String eventType, String consumerName, 
                                        Runnable businessLogic) {
        // 1. 멱등성 체크
        if (isEventAlreadyHandled(eventId, consumerName)) {
            return false; // 이미 처리됨
        }

        // 2. 비즈니스 로직 실행
        try {
            businessLogic.run();
            
            // 3. 처리 완료 기록
            markEventAsHandled(eventId, eventType, consumerName);
            
            log.info("Event processed successfully - EventId: {}, Consumer: {}", eventId, consumerName);
            return true; // 새로 처리됨
            
        } catch (Exception e) {
            log.error("Failed to process event - EventId: {}, Consumer: {}, Error: {}", 
                    eventId, consumerName, e.getMessage(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }
}
