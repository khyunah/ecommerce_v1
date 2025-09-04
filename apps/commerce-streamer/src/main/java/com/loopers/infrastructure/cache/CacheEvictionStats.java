package com.loopers.infrastructure.cache;

import lombok.Builder;
import lombok.Getter;

/**
 * 캐시 삭제 통계 정보
 */
@Getter
@Builder
public class CacheEvictionStats {
    
    private final int productDetailCacheCount;
    private final int productListCacheCount;
    private final int popularProductsCacheCount;
    private final int productSearchCacheCount;
    
    public int getTotalCacheCount() {
        return productDetailCacheCount + productListCacheCount + 
               popularProductsCacheCount + productSearchCacheCount;
    }
}
