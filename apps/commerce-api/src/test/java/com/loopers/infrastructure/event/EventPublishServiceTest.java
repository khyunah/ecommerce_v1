package com.loopers.infrastructure.event;

import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.ProductViewedEvent;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import com.loopers.domain.event.order.OrderPaidEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublishServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private EventPublishService eventPublishService;

    @Test
    void 재고_변경_이벤트_발행() {
        // given
        Long productId = 1L;
        Long previousStock = 10L;
        Long currentStock = 8L;
        String reason = "구매";

        // when
        eventPublishService.publishStockAdjusted(productId, previousStock, currentStock, reason);

        // then
        verify(eventPublisher).publishCatalogEvent(any(StockAdjustedEvent.class));
    }

    @Test
    void 좋아요_변경_이벤트_발행() {
        // given
        Long productId = 1L;
        Long userId = 100L;
        Long previousCount = 5L;
        Long currentCount = 6L;
        String action = "LIKED";

        // when
        eventPublishService.publishLikeChanged(productId, userId, previousCount, currentCount, action);

        // then
        verify(eventPublisher).publishCatalogEvent(any(LikeChangedEvent.class));
    }

    @Test
    void 상품_조회_이벤트_발행() {
        // given
        Long productId = 1L;
        Long userId = 100L;
        String sessionId = "session-123";

        // when
        eventPublishService.publishProductViewed(productId, userId, sessionId);

        // then
        verify(eventPublisher).publishCatalogEvent(any(ProductViewedEvent.class));
    }

    @Test
    void 주문_결제_완료_이벤트_발행() {
        // given
        Long orderId = 1L;
        Long userId = 100L;
        BigDecimal totalAmount = BigDecimal.valueOf(50000);
        List<OrderPaidEvent.OrderItem> orderItems = List.of(
            new OrderPaidEvent.OrderItem(1L, "상품1", 2L, BigDecimal.valueOf(25000))
        );

        // when
        eventPublishService.publishOrderPaid(orderId, userId, totalAmount, orderItems);

        // then
        verify(eventPublisher).publishOrderEvent(any(OrderPaidEvent.class));
    }
}
