package com.loopers.infrastructure.event;

import com.loopers.domain.event.catalog.LikeChangedEvent;
import com.loopers.domain.event.catalog.ProductViewedEvent;
import com.loopers.domain.event.catalog.StockAdjustedEvent;
import com.loopers.domain.event.order.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 비즈니스 로직에서 사용할 이벤트 발행 서비스
 * 도메인 이벤트 생성과 발행을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublishService {

    private final EventPublisher eventPublisher;

    /**
     * 재고 변경 이벤트 발행
     */
    public void publishStockAdjusted(Long productId, Long previousStock, Long currentStock, String reason) {
        StockAdjustedEvent event = new StockAdjustedEvent(productId, previousStock, currentStock, reason);
        eventPublisher.publishCatalogEvent(event);
        
        log.debug("Published StockAdjusted event - ProductId: {}, CurrentStock: {}, Reason: {}", 
                productId, currentStock, reason);
    }

    /**
     * 좋아요 변경 이벤트 발행
     */
    public void publishLikeChanged(Long productId, Long userId, Long previousCount, Long currentCount, String action) {
        LikeChangedEvent event = new LikeChangedEvent(productId, userId, previousCount, currentCount, action);
        eventPublisher.publishCatalogEvent(event);
        
        log.debug("Published LikeChanged event - ProductId: {}, Action: {}, CurrentCount: {}", 
                productId, action, currentCount);
    }

    /**
     * 상품 조회 이벤트 발행
     */
    public void publishProductViewed(Long productId, Long userId, String sessionId) {
        ProductViewedEvent event = new ProductViewedEvent(productId, userId, sessionId);
        eventPublisher.publishCatalogEvent(event);
        
        log.debug("Published ProductViewed event - ProductId: {}, UserId: {}", 
                productId, userId);
    }

    /**
     * 주문 결제 완료 이벤트 발행
     */
    public void publishOrderPaid(Long orderId, Long userId, BigDecimal totalAmount, 
                                List<OrderPaidEvent.OrderItem> orderItems) {
        OrderPaidEvent event = new OrderPaidEvent(orderId, userId, totalAmount, orderItems);
        eventPublisher.publishOrderEvent(event);
        
        log.debug("Published OrderPaid event - OrderId: {}, TotalAmount: {}, ItemCount: {}", 
                orderId, totalAmount, orderItems.size());
    }
}
