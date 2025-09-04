package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private EventHandledRepository eventHandledRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void 이미_처리된_이벤트_확인() {
        // given
        String eventId = "event-123";
        String consumerName = "TestConsumer";
        
        when(eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName))
            .thenReturn(Optional.of(mock(EventHandled.class)));

        // when
        boolean isHandled = idempotencyService.isEventAlreadyHandled(eventId, consumerName);

        // then
        assertThat(isHandled).isTrue();
    }

    @Test
    void 처리되지_않은_이벤트_확인() {
        // given
        String eventId = "event-123";
        String consumerName = "TestConsumer";
        
        when(eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName))
            .thenReturn(Optional.empty());

        // when
        boolean isHandled = idempotencyService.isEventAlreadyHandled(eventId, consumerName);

        // then
        assertThat(isHandled).isFalse();
    }

    @Test
    void 이벤트_처리_완료_기록() {
        // given
        String eventId = "event-123";
        String eventType = "TestEvent";
        String consumerName = "TestConsumer";
        
        when(eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName))
            .thenReturn(Optional.empty());

        // when
        idempotencyService.markEventAsHandled(eventId, eventType, consumerName);

        // then
        verify(eventHandledRepository).save(any(EventHandled.class));
    }

    @Test
    void 이미_기록된_이벤트_중복_기록_시도() {
        // given
        String eventId = "event-123";
        String eventType = "TestEvent";
        String consumerName = "TestConsumer";
        
        when(eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName))
            .thenReturn(Optional.of(mock(EventHandled.class)));

        // when
        idempotencyService.markEventAsHandled(eventId, eventType, consumerName);

        // then
        verify(eventHandledRepository, never()).save(any(EventHandled.class));
    }

    @Test
    void 멱등성_처리와_비즈니스_로직_실행_성공() {
        // given
        String eventId = "event-123";
        String eventType = "TestEvent";
        String consumerName = "TestConsumer";
        
        when(eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName))
            .thenReturn(Optional.empty());

        AtomicBoolean businessLogicExecuted = new AtomicBoolean(false);
        Runnable businessLogic = () -> businessLogicExecuted.set(true);

        // when
        boolean processed = idempotencyService.processWithIdempotency(
            eventId, eventType, consumerName, businessLogic);

        // then
        assertThat(processed).isTrue();
        assertThat(businessLogicExecuted.get()).isTrue();
        verify(eventHandledRepository).save(any(EventHandled.class));
    }

    @Test
    void 이미_처리된_이벤트_비즈니스_로직_실행_안함() {
        // given
        String eventId = "event-123";
        String eventType = "TestEvent";
        String consumerName = "TestConsumer";
        
        when(eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName))
            .thenReturn(Optional.of(mock(EventHandled.class)));

        AtomicBoolean businessLogicExecuted = new AtomicBoolean(false);
        Runnable businessLogic = () -> businessLogicExecuted.set(true);

        // when
        boolean processed = idempotencyService.processWithIdempotency(
            eventId, eventType, consumerName, businessLogic);

        // then
        assertThat(processed).isFalse();
        assertThat(businessLogicExecuted.get()).isFalse();
        verify(eventHandledRepository, never()).save(any(EventHandled.class));
    }

    @Test
    void 비즈니스_로직_실행_실패_시_예외_재발생() {
        // given
        String eventId = "event-123";
        String eventType = "TestEvent";
        String consumerName = "TestConsumer";
        
        when(eventHandledRepository.findByEventIdAndConsumerName(eventId, consumerName))
            .thenReturn(Optional.empty());

        RuntimeException testException = new RuntimeException("Business logic failed");
        Runnable businessLogic = () -> { throw testException; };

        // when & then
        assertThatThrownBy(() -> idempotencyService.processWithIdempotency(
            eventId, eventType, consumerName, businessLogic))
            .isEqualTo(testException);

        verify(eventHandledRepository, never()).save(any(EventHandled.class));
    }
}
