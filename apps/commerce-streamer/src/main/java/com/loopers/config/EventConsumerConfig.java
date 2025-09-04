package com.loopers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.ProductViewedEvent;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import com.loopers.domain.event.order.OrderPaidEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Consumer용 이벤트 설정
 * JSON 역직렬화를 위한 ObjectMapper 설정
 */
@Configuration
public class EventConsumerConfig {

    @Bean("eventObjectMapper")
    public ObjectMapper eventObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 Time API 지원
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 이벤트 타입 등록 (역직렬화를 위해)
        mapper.registerSubtypes(
            StockAdjustedEvent.class,
            LikeChangedEvent.class,
            ProductViewedEvent.class,
            OrderPaidEvent.class
        );
        
        return mapper;
    }
}
