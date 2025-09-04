package com.loopers.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import com.loopers.infrastructure.cache.ProductCacheEvictService;
import com.loopers.infrastructure.event.IdempotencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CacheEvictConsumerTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductCacheEvictService cacheEvictService;

    @InjectMocks
    private CacheEvictConsumer cacheEvictConsumer;

    @Test
    void 재고_변경_이벤트_처리_일반_재고_변경() {
        // given - 일반적인 재고 변경 (재고 소진 아님)
        StockAdjustedEvent event = new StockAdjustedEvent(1L, 10L, 8L, "구매");

        // when
        cacheEvictConsumer.processEvent(event);

        // then
        verify(cacheEvictService).evictProductDetailCache(1L);
        // 재고 소진이 아니므로 목록 캐시는 삭제하지 않음
    }

    @Test
    void 재고_변경_이벤트_처리_재고_소진() {
        // given - 재고 소진 상황
        StockAdjustedEvent event = new StockAdjustedEvent(1L, 1L, 0L, "구매");

        // when
        cacheEvictConsumer.processEvent(event);

        // then
        verify(cacheEvictService).evictProductDetailCache(1L);
        verify(cacheEvictService).evictProductListCaches(1L);
        verify(cacheEvictService).evictPopularProductsCaches();
    }

    @Test
    void 좋아요_변경_이벤트_처리() {
        // given
        LikeChangedEvent event = new LikeChangedEvent(1L, 100L, 5L, 6L, "LIKED");

        // when
        cacheEvictConsumer.processEvent(event);

        // then
        verify(cacheEvictService).evictProductDetailCache(1L);
        verify(cacheEvictService).evictPopularProductsCaches();
    }

    @Test
    void Consumer_이름_반환() {
        // when
        String consumerName = cacheEvictConsumer.getConsumerName();

        // then
        assertThat(consumerName).isEqualTo("CacheEvictConsumer");
    }
}
