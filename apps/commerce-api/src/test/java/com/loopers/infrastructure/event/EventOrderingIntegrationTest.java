package com.loopers.infrastructure.event;

import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.ProductViewedEvent;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 이벤트 순서 보장 단위 테스트
 * Spring Boot 컨텍스트 없이 파티션 키 기반 순서 보장 로직 검증
 */
class EventOrderingTest {

    @Test
    void 동일한_상품ID의_이벤트들은_같은_파티션_키를_가진다() {
        // given
        Long productId = 1L;
        List<String> partitionKeys = new ArrayList<>();
        
        StockAdjustedEvent stockEvent = new StockAdjustedEvent(productId, 10L, 8L, "구매");
        LikeChangedEvent likeEvent = new LikeChangedEvent(productId, 100L, 5L, 6L, "LIKED");
        ProductViewedEvent viewEvent = new ProductViewedEvent(productId, 200L, "session-123");

        // when
        partitionKeys.add(stockEvent.getPartitionKey());
        partitionKeys.add(likeEvent.getPartitionKey());
        partitionKeys.add(viewEvent.getPartitionKey());

        // then
        assertThat(partitionKeys).hasSize(3);
        assertThat(partitionKeys.get(0)).isEqualTo(productId.toString());
        assertThat(partitionKeys.get(1)).isEqualTo(productId.toString());
        assertThat(partitionKeys.get(2)).isEqualTo(productId.toString());
        
        // 모든 이벤트가 같은 파티션 키를 가지므로 순서가 보장됨
        assertThat(partitionKeys.stream().distinct().count()).isEqualTo(1);
    }

    @Test
    void 다른_상품ID의_이벤트들은_다른_파티션에_분산된다() {
        // given
        StockAdjustedEvent product1Event = new StockAdjustedEvent(1L, 10L, 8L, "구매");
        StockAdjustedEvent product2Event = new StockAdjustedEvent(2L, 5L, 3L, "구매");
        StockAdjustedEvent product3Event = new StockAdjustedEvent(3L, 20L, 18L, "구매");

        // when
        String partitionKey1 = product1Event.getPartitionKey();
        String partitionKey2 = product2Event.getPartitionKey();
        String partitionKey3 = product3Event.getPartitionKey();

        // then
        assertThat(partitionKey1).isEqualTo("1");
        assertThat(partitionKey2).isEqualTo("2");
        assertThat(partitionKey3).isEqualTo("3");
        
        // 모두 다른 파티션 키를 가지므로 병렬 처리 가능
        List<String> allKeys = List.of(partitionKey1, partitionKey2, partitionKey3);
        assertThat(allKeys.stream().distinct().count()).isEqualTo(3);
    }

    @Test
    void 동시_발행된_같은_상품_이벤트들의_파티션_키_일관성() throws ExecutionException, InterruptedException {
        // given
        Long productId = 1L;
        int numberOfEvents = 100;
        
        // when - 동시에 여러 이벤트 생성
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfEvents; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                StockAdjustedEvent event = new StockAdjustedEvent(productId, 10L, (long) (10 - Math.random() * 5), "구매");
                return event.getPartitionKey();
            }));
        }
        
        // then
        List<String> partitionKeys = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            partitionKeys.add(future.get());
        }
        
        // 모든 이벤트가 같은 파티션 키를 가져야 함
        assertThat(partitionKeys).hasSize(numberOfEvents);
        assertThat(partitionKeys.stream().distinct().count()).isEqualTo(1);
        assertThat(partitionKeys.get(0)).isEqualTo(productId.toString());
    }
}
