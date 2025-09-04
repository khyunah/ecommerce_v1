package com.loopers.infrastructure.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCacheEvictServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private ProductCacheEvictService cacheEvictService;

    @Test
    void 상품_상세_캐시_삭제_성공() {
        // given
        Long productId = 1L;
        String expectedKey = CacheKeyStrategy.productDetail(productId);
        
        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        // when
        cacheEvictService.evictProductDetailCache(productId);

        // then
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void 상품_상세_캐시_삭제_대상_없음() {
        // given
        Long productId = 1L;
        String expectedKey = CacheKeyStrategy.productDetail(productId);
        
        when(redisTemplate.delete(expectedKey)).thenReturn(false);

        // when
        cacheEvictService.evictProductDetailCache(productId);

        // then
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void 상품_목록_캐시_삭제() {
        // given
        Long productId = 1L;
        String pattern = CacheKeyStrategy.productListPattern();
        Set<String> mockKeys = Set.of(
            "product:list:electronics:popular:0:10",
            "product:list:fashion:newest:0:20"
        );
        
        when(redisTemplate.keys(pattern)).thenReturn(mockKeys);
        when(redisTemplate.delete(mockKeys)).thenReturn(2L);

        // when
        cacheEvictService.evictProductListCaches(productId);

        // then
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(mockKeys);
    }

    @Test
    void 인기_상품_캐시_삭제() {
        // given
        String pattern = CacheKeyStrategy.popularProductsPattern();
        Set<String> mockKeys = Set.of(
            "product:popular:daily:10",
            "product:popular:weekly:20"
        );
        
        when(redisTemplate.keys(pattern)).thenReturn(mockKeys);
        when(redisTemplate.delete(mockKeys)).thenReturn(2L);

        // when
        cacheEvictService.evictPopularProductsCaches();

        // then
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(mockKeys);
    }

    @Test
    void 모든_관련_캐시_삭제() {
        // given
        Long productId = 1L;
        
        // 상품 상세 캐시 삭제
        String detailKey = CacheKeyStrategy.productDetail(productId);
        when(redisTemplate.delete(detailKey)).thenReturn(true);
        
        // 목록 캐시 삭제
        when(redisTemplate.keys(CacheKeyStrategy.productListPattern())).thenReturn(Set.of("list:key1"));
        when(redisTemplate.delete(Set.of("list:key1"))).thenReturn(1L);
        
        // 인기 상품 캐시 삭제
        when(redisTemplate.keys(CacheKeyStrategy.popularProductsPattern())).thenReturn(Set.of("popular:key1"));
        when(redisTemplate.delete(Set.of("popular:key1"))).thenReturn(1L);
        
        // 검색 캐시 삭제
        when(redisTemplate.keys(CacheKeyStrategy.searchPattern())).thenReturn(Set.of("search:key1"));
        when(redisTemplate.delete(Set.of("search:key1"))).thenReturn(1L);

        // when
        cacheEvictService.evictAllProductRelatedCaches(productId);

        // then
        verify(redisTemplate).delete(detailKey);
        verify(redisTemplate, times(3)).keys(anyString());
        verify(redisTemplate, times(3)).delete(any(Set.class));
    }

    @Test
    void 캐시_삭제_결과_확인() {
        // given
        String cacheKey = "test:key";
        
        when(redisTemplate.hasKey(cacheKey)).thenReturn(false); // 삭제됨

        // when
        boolean isEvicted = cacheEvictService.isCacheEvicted(cacheKey);

        // then
        assertThat(isEvicted).isTrue();
    }

    @Test
    void 캐시_통계_정보_조회() {
        // given
        when(redisTemplate.keys(CacheKeyStrategy.productDetailPattern()))
            .thenReturn(Set.of("detail:1", "detail:2"));
        when(redisTemplate.keys(CacheKeyStrategy.productListPattern()))
            .thenReturn(Set.of("list:1"));
        when(redisTemplate.keys(CacheKeyStrategy.popularProductsPattern()))
            .thenReturn(Set.of("popular:1", "popular:2", "popular:3"));
        when(redisTemplate.keys(CacheKeyStrategy.searchPattern()))
            .thenReturn(Set.of());

        // when
        CacheEvictionStats stats = cacheEvictService.getCacheEvictionStats();

        // then
        assertThat(stats.getProductDetailCacheCount()).isEqualTo(2);
        assertThat(stats.getProductListCacheCount()).isEqualTo(1);
        assertThat(stats.getPopularProductsCacheCount()).isEqualTo(3);
        assertThat(stats.getProductSearchCacheCount()).isEqualTo(0);
        assertThat(stats.getTotalCacheCount()).isEqualTo(6);
    }
}
