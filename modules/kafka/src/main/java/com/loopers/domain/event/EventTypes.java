package com.loopers.domain.event;

/**
 * 이벤트 타입 상수 관리
 */
public final class EventTypes {
    
    // Catalog Events
    public static final String STOCK_ADJUSTED = "StockAdjusted";
    public static final String LIKE_CHANGED = "LikeChanged";
    public static final String PRODUCT_VIEWED = "ProductViewed";
    
    // Order Events
    public static final String ORDER_PAID = "OrderPaid";
    
    private EventTypes() {
        // 유틸리티 클래스
    }
}
