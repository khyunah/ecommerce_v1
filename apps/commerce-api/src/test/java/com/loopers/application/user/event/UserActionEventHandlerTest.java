package com.loopers.application.user.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.user.event.UserActionEvent;
import com.loopers.domain.user.event.UserActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActionEventHandlerTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private UserActionLogger userActionLogger;
    
    @Mock
    private UserActionAnalyzer userActionAnalyzer;
    
    @InjectMocks
    private UserActionEventHandler userActionEventHandler;

    @DisplayName("사용자 행동 이벤트 처리 시 모든 처리 단계가 실행된다")
    @Test
    void should_execute_all_processing_steps_when_handling_user_action_event() throws Exception {
        // Given
        UserActionEvent event = createTestEvent();
        
        given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":\"test\"}");
        
        // When
        userActionEventHandler.handleUserActionEvent(event);
        
        // Then
        // 1. JSON 변환 확인
        verify(objectMapper).writeValueAsString(event);
        
        // 2. 분석 데이터베이스 저장 확인
        verify(userActionLogger).save(event);
        
        // 3. 외부 분석 시스템 전송 확인
        verify(userActionAnalyzer).sendToExternalSystems(event);
        
        // 4. 실시간 분석 처리 확인
        verify(userActionAnalyzer).processRealTime(event);
    }

    @DisplayName("데이터베이스 저장 실패해도 다른 처리는 계속 진행된다")
    @Test
    void should_continue_processing_even_when_database_save_fails() throws Exception {
        // Given
        UserActionEvent event = createTestEvent();
        
        given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":\"test\"}");
        
        // 데이터베이스 저장 실패 시뮬레이션
        doThrow(new RuntimeException("Database save failed")).when(userActionLogger).save(any());
        
        // When
        userActionEventHandler.handleUserActionEvent(event);
        
        // Then
        // 데이터베이스 저장은 실패했지만 다른 처리는 계속 진행
        verify(objectMapper).writeValueAsString(event);
        verify(userActionAnalyzer).sendToExternalSystems(event);
        verify(userActionAnalyzer).processRealTime(event);
    }

    @DisplayName("외부 분석 시스템 전송 실패해도 다른 처리는 계속 진행된다")
    @Test
    void should_continue_processing_even_when_external_analytics_fails() throws Exception {
        // Given
        UserActionEvent event = createTestEvent();
        
        given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":\"test\"}");
        
        // 외부 분석 시스템 전송 실패 시뮬레이션
        doThrow(new RuntimeException("External analytics failed")).when(userActionAnalyzer).sendToExternalSystems(any());
        
        // When
        userActionEventHandler.handleUserActionEvent(event);
        
        // Then
        // 외부 시스템 전송은 실패했지만 다른 처리는 계속 진행
        verify(objectMapper).writeValueAsString(event);
        verify(userActionLogger).save(event);
        verify(userActionAnalyzer).processRealTime(event);
    }

    @DisplayName("실시간 분석 처리 실패해도 전체 처리는 정상 완료된다")
    @Test
    void should_complete_processing_even_when_real_time_analytics_fails() throws Exception {
        // Given
        UserActionEvent event = createTestEvent();
        
        given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":\"test\"}");
        
        // 실시간 분석 처리 실패 시뮬레이션
        doThrow(new RuntimeException("Real-time analytics failed")).when(userActionAnalyzer).processRealTime(any());
        
        // When
        userActionEventHandler.handleUserActionEvent(event);
        
        // Then
        // 실시간 분석 처리는 실패했지만 다른 처리는 모두 완료
        verify(objectMapper).writeValueAsString(event);
        verify(userActionLogger).save(event);
        verify(userActionAnalyzer).sendToExternalSystems(event);
    }

    private UserActionEvent createTestEvent() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "ELECTRONICS");
        
        return new UserActionEvent(
                123L,           // userId
                "SESSION_123",  // sessionId
                UserActionType.PRODUCT_VIEW,
                "PRODUCT",
                "456",
                properties,
                "Mozilla/5.0",
                "192.168.1.1",
                "https://example.com"
        );
    }
}
