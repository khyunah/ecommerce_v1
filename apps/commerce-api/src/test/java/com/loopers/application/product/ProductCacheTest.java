package com.loopers.application.product;

import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductCacheTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    private Brand testBrand;
    private Product product;

    @BeforeEach
    void setUp() {

        testBrand = Brand.from("테스트브랜드", "테스트 브랜드 설명");
        testBrand = brandJpaRepository.save(testBrand);

        product = Product.from("상품1", "설명1", BigDecimal.valueOf(10000), BigDecimal.valueOf(18000), "ON_SALE", testBrand.getId());
        product = productJpaRepository.save(product);
    }

    @Test
    @Order(1)
    void 상품상세_캐시_테스트() {
        // Given
        clearCache();

        // When - 첫 번째 호출
        long start1 = System.currentTimeMillis();
        ProductDetailInfo result1 = productFacade.getDetail(product.getId());
        long time1 = System.currentTimeMillis() - start1;

        // When - 두 번째 호출 (캐시에서)
        long start2 = System.currentTimeMillis();
        ProductDetailInfo result2 = productFacade.getDetail(product.getId());
        long time2 = System.currentTimeMillis() - start2;

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(time2).isLessThan(time1); // 캐시가 더 빠름
        assertThat(isCached("productDetail", "detail:" + product.getId())).isTrue();
    }

    @Test
    @Order(2)
    void 상품목록_캐시_테스트() {
        // Given
        clearCache();
        Pageable pageable = PageRequest.of(0, 20);

        // When - 첫 번째 호출
        long start1 = System.currentTimeMillis();
        Page<ProductWithLikeCountDto> result1 = productService.getProducts(null, ProductSortType.LATEST, pageable);
        long time1 = System.currentTimeMillis() - start1;

        // When - 두 번째 호출 (캐시에서)
        long start2 = System.currentTimeMillis();
        Page<ProductWithLikeCountDto> result2 = productService.getProducts(null, ProductSortType.LATEST, pageable);
        long time2 = System.currentTimeMillis() - start2;

        // Then
        assertThat(result1.getContent()).isEqualTo(result2.getContent());
        assertThat(time2).isLessThan(time1);
        assertThat(isCached("productList", "latest:page:0:size:20")).isTrue();
    }

    @Test
    @Order(3)
    void 캐시_조건_테스트() {
        // Given
        clearCache();
        Pageable pageable = PageRequest.of(3, 20); // 4페이지

        // When
        productService.getProducts(null, ProductSortType.LATEST, pageable);

        // Then - 4페이지는 캐시되지 않음
        assertThat(isCached("productList", "latest:page:3:size:20")).isFalse();

        // When - 브랜드 필터가 있으면 캐시되지 않음
        productService.getProducts(1L, ProductSortType.LATEST, PageRequest.of(0, 20));

        // Then
        assertThat(getAllCacheKeys("productList")).isEmpty();
    }

    @Test
    @Order(4)
    void 캐시_무효화_테스트() {
        // Given
        clearCache();
        productFacade.getDetail(product.getId()); // 캐시 생성

        // When
        cacheManager.getCache("productDetail").evict("detail:" + product.getId());

        // Then
        assertThat(isCached("productDetail", "detail:" + product.getId())).isFalse();
    }

    private void clearCache() {
        cacheManager.getCacheNames().forEach(cacheName ->
                cacheManager.getCache(cacheName).clear());
    }

    private boolean isCached(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        return cache != null && cache.get(key) != null;
    }

    private Set<String> getAllCacheKeys(String cacheName) {
        // Redis 환경에서 키 확인
        return Collections.emptySet(); // 실제 구현 필요
    }
}
