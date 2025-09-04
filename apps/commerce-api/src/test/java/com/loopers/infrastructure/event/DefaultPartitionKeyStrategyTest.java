package com.loopers.infrastructure.event;

import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import com.loopers.domain.event.order.OrderPaidEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPartitionKeyStrategyTest {

    private final DefaultPartitionKeyStrategy strategy = new DefaultPartitionKeyStrategy();

    @Test
    void 상품_이벤트의_파티션_키_생성() {
        // given
        StockAdjustedEvent stockEvent = new StockAdjustedEvent(1L, 10L, 8L, "구매");
        LikeChangedEvent likeEvent = new LikeChangedEvent(1L, 100L, 5L, 6L, "LIKED");

        // when
        String stockPartitionKey = strategy.generatePartitionKey(stockEvent);
        String likePartitionKey = strategy.generatePartitionKey(likeEvent);

        // then
        assertThat(stockPartitionKey).isEqualTo("1"); // productId
        assertThat(likePartitionKey).isEqualTo("1");  // 동일한 productId
        assertThat(stockPartitionKey).isEqualTo(likePartitionKey); // 순서 보장
    }

    @Test
    void 주문_이벤트의_파티션_키_생성() {
        // given
        OrderPaidEvent orderEvent = new OrderPaidEvent(
            100L, 200L, BigDecimal.valueOf(50000), 
            List.of(new OrderPaidEvent.OrderItem(1L, "상품1", 2L, BigDecimal.valueOf(25000)))
        );

        // when
        String partitionKey = strategy.generatePartitionKey(orderEvent);

        // then
        assertThat(partitionKey).isEqualTo("100"); // orderId
    }

    @Test
    void 다른_상품의_이벤트는_다른_파티션_키() {
        // given
        StockAdjustedEvent product1Event = new StockAdjustedEvent(1L, 10L, 8L, "구매");
        StockAdjustedEvent product2Event = new StockAdjustedEvent(2L, 5L, 3L, "구매");

        // when
        String partitionKey1 = strategy.generatePartitionKey(product1Event);
        String partitionKey2 = strategy.generatePartitionKey(product2Event);

        // then
        assertThat(partitionKey1).isEqualTo("1");
        assertThat(partitionKey2).isEqualTo("2");
        assertThat(partitionKey1).isNotEqualTo(partitionKey2); // 다른 파티션
    }

    @Test
    void 파티션_키_유효성_검증() {
        // given
        String validKey = "product:123";
        String nullKey = null;
        String emptyKey = "";
        String tooLongKey = "a".repeat(256);
        String keyWithNewline = "product:123\n";

        // when & then
        assertThat(strategy.isValidPartitionKey(validKey)).isTrue();
        assertThat(strategy.isValidPartitionKey(nullKey)).isFalse();
        assertThat(strategy.isValidPartitionKey(emptyKey)).isFalse();
        assertThat(strategy.isValidPartitionKey(tooLongKey)).isFalse();
        assertThat(strategy.isValidPartitionKey(keyWithNewline)).isFalse();
    }

    @Test
    void 접두사_추가_기능() {
        // when
        String productKey = strategy.createProductPartitionKey(123L);
        String orderKey = strategy.createOrderPartitionKey(456L);

        // then
        assertThat(productKey).isEqualTo("product:123");
        assertThat(orderKey).isEqualTo("order:456");
    }

    @Test
    void 접두사가_없어도_처리() {
        // when
        String keyWithoutPrefix = strategy.addPrefix(null, "123");
        String keyWithEmptyPrefix = strategy.addPrefix("", "123");

        // then
        assertThat(keyWithoutPrefix).isEqualTo("123");
        assertThat(keyWithEmptyPrefix).isEqualTo("123");
    }
}
