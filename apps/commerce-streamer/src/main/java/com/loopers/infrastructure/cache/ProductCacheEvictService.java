package com.loopers.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 상품 캐시 무효화 서비스
 * Redis에서 상품 관련 캐시를 삭제하는 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheEvictService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 상품 상세 캐시 삭제
     */
    public void evictProductDetailCache(Long productId) {
        String cacheKey = CacheKeyStrategy.productDetail(productId);
        
        Boolean deleted = redisTemplate.delete(cacheKey);
        
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("Evicted product detail cache - ProductId: {}, Key: {}", productId, cacheKey);
        } else {
            log.debug("Product detail cache not found or already expired - ProductId: {}, Key: {}", 
                    productId, cacheKey);
        }
    }

    /**
     * 상품 목록 캐시 삭제 (특정 상품이 포함된 목록들)
     */
    public void evictProductListCaches(Long productId) {
        // 패턴 매칭으로 관련 목록 캐시 찾아서 삭제
        String pattern = CacheKeyStrategy.productListPattern();
        
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keys);
            log.debug("Evicted product list caches - ProductId: {}, DeletedCount: {}", productId, deletedCount);
        }
    }

    /**
     * 인기 상품 캐시 삭제
     */
    public void evictPopularProductsCaches() {
        String pattern = CacheKeyStrategy.popularProductsPattern();
        
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keys);
            log.debug("Evicted popular products caches - DeletedCount: {}", deletedCount);
        }
    }

    /**
     * 상품 검색 캐시 삭제 (특정 상품 관련)
     */
    public void evictProductSearchCaches(Long productId) {
        String pattern = CacheKeyStrategy.searchPattern();
        
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keys);
            log.debug("Evicted product search caches - ProductId: {}, DeletedCount: {}", productId, deletedCount);
        }
    }

    /**
     * 특정 상품과 관련된 모든 캐시 삭제 (종합 삭제)
     */
    public void evictAllProductRelatedCaches(Long productId) {
        evictProductDetailCache(productId);
        evictProductListCaches(productId);
        evictPopularProductsCaches();
        evictProductSearchCaches(productId);
        
        log.info("Evicted all caches related to ProductId: {}", productId);
    }

    /**
     * 캐시 삭제 결과 확인
     */
    public boolean isCacheEvicted(String cacheKey) {
        Boolean exists = redisTemplate.hasKey(cacheKey);
        return !Boolean.TRUE.equals(exists);
    }

    /**
     * 캐시 통계 정보
     */
    public CacheEvictionStats getCacheEvictionStats() {
        // 각 타입별 캐시 개수 조회
        Set<String> productDetailKeys = redisTemplate.keys(CacheKeyStrategy.productDetailPattern());
        Set<String> productListKeys = redisTemplate.keys(CacheKeyStrategy.productListPattern());
        Set<String> popularProductsKeys = redisTemplate.keys(CacheKeyStrategy.popularProductsPattern());
        Set<String> productSearchKeys = redisTemplate.keys(CacheKeyStrategy.searchPattern());

        return CacheEvictionStats.builder()
            .productDetailCacheCount(productDetailKeys != null ? productDetailKeys.size() : 0)
            .productListCacheCount(productListKeys != null ? productListKeys.size() : 0)
            .popularProductsCacheCount(popularProductsKeys != null ? popularProductsKeys.size() : 0)
            .productSearchCacheCount(productSearchKeys != null ? productSearchKeys.size() : 0)
            .build();
    }
}
