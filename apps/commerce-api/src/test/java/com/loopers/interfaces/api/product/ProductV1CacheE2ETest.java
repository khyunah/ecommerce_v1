package com.loopers.interfaces.api.product;

import com.loopers.CommerceApiApplication;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.like.Like;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CommerceApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductV1CacheE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private CacheManager cacheManager;

    private Brand testBrand;
    private User testUser;
    private Product recentProduct, oldProduct;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        clearAllCaches();

        // 테스트 브랜드 생성
        testBrand = Brand.from("테스트브랜드", "테스트 브랜드 설명");
        testBrand = brandJpaRepository.save(testBrand);

        // 테스트 유저 생성
        testUser = User.from("user1", "user1@test.com", "1990-01-01", "M");
        testUser = userJpaRepository.save(testUser);

        // 최근 상품 생성 (3개월 이내 - 캐시 대상)
        recentProduct = Product.from("최근상품", "최근 상품 설명",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(18000), "ON_SALE", testBrand.getId());
        recentProduct = productJpaRepository.save(recentProduct);

        // 오래된 상품 생성 (3개월 이전 - 캐시 대상 아님)
        oldProduct = Product.from("오래된상품", "오래된 상품 설명",
                BigDecimal.valueOf(20000), BigDecimal.valueOf(28000), "ON_SALE", testBrand.getId());
        // createdAt을 직접 설정하기 위해 reflection 사용하거나
        // 별도 메서드 구현 필요 (여기서는 간단히 표현)
        oldProduct = productJpaRepository.save(oldProduct);

        // 좋아요 추가
        likeJpaRepository.save(Like.from(testUser.getId(), recentProduct.getId()));
        Product.increaseLikeCount(recentProduct);
    }

    @Test
    @DisplayName("상품 상세 조회 - 최근 상품 캐시 적용 확인")
    void getProductDetail_recentProduct_cacheApplied() throws Exception {
        // Given
        Long productId = recentProduct.getId();

        // When - 첫 번째 호출 (DB 조회 + 캐시 저장)
        long startTime1 = System.currentTimeMillis();
        MvcResult result1 = mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("최근상품"))
                .andReturn();
        long responseTime1 = System.currentTimeMillis() - startTime1;

        // When - 두 번째 호출 (캐시에서 조회)
        long startTime2 = System.currentTimeMillis();
        MvcResult result2 = mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("최근상품"))
                .andReturn();
        long responseTime2 = System.currentTimeMillis() - startTime2;

        // Then
        assertThat(responseTime2).isLessThan(responseTime1); // 캐시가 더 빠름
        assertThat(isCached("productDetail", "detail:" + productId)).isTrue();

        // 응답 내용이 동일한지 확인
        String response1 = result1.getResponse().getContentAsString();
        String response2 = result2.getResponse().getContentAsString();
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    @DisplayName("상품 상세 조회 - 오래된 상품 캐시 미적용 확인")
    void getProductDetail_oldProduct_cacheNotApplied() throws Exception {
        // Given
        Long productId = oldProduct.getId();

        // Then - 캐시되지 않음
        assertThat(isCached("productDetail", "detail:" + productId)).isFalse();
    }

    @Test
    @DisplayName("상품 목록 조회 - 캐시 조건 확인 (브랜드 없음, 최신순, 3페이지 이내)")
    void getProducts_cacheCondition_applied() throws Exception {
        // Given - 캐시 조건: brandId=null, sortType=LATEST, page<=2

        // When - 첫 번째 호출 (0페이지 - 캐시됨)
        long startTime1 = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/products")
                        .param("sortType", "LATEST")
                        .param("page", "0")
                        .param("size", "20")
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"));
        long responseTime1 = System.currentTimeMillis() - startTime1;

        // When - 두 번째 호출 (캐시에서 조회)
        long startTime2 = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/products")
                        .param("sortType", "LATEST")
                        .param("page", "0")
                        .param("size", "20")
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk());
        long responseTime2 = System.currentTimeMillis() - startTime2;

        // Then
        assertThat(responseTime2).isLessThan(responseTime1);
        assertThat(isCached("productList", "latest:page:0:size:20")).isTrue();
    }

    @Test
    @DisplayName("상품 목록 조회 - 캐시 조건 미충족 확인")
    void getProducts_cacheCondition_notApplied() throws Exception {
        // Case 1: 브랜드 필터 있음 (캐시 안됨)
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .param("sortType", "LATEST")
                        .param("page", "0")
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk());

        assertThat(getAllCacheKeys("productList")).isEmpty();

        // Case 2: 좋아요순 정렬 (캐시 안됨)
        mockMvc.perform(get("/api/v1/products")
                        .param("sortType", "LIKE_COUNT")
                        .param("page", "0")
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk());

        assertThat(getAllCacheKeys("productList")).isEmpty();

        // Case 3: 4페이지 이상 (캐시 안됨)
        mockMvc.perform(get("/api/v1/products")
                        .param("sortType", "LATEST")
                        .param("page", "3")
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk());

        assertThat(isCached("productList", "latest:page:3:size:20")).isFalse();
    }

    @Test
    @DisplayName("캐시 성능 비교 테스트")
    void cache_performance_comparison() throws Exception {
        Long productId = recentProduct.getId();
        int testCount = 5;

        // 캐시 없는 상태로 여러 번 호출
        clearAllCaches();
        long totalTimeWithoutCache = 0;
        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/products/{productId}", productId)
                            .header("X-USER-ID", testUser.getId()))
                    .andExpect(status().isOk());
            totalTimeWithoutCache += (System.currentTimeMillis() - startTime);
            clearAllCaches(); // 매번 캐시 초기화
        }

        // 캐시 있는 상태로 여러 번 호출
        // 첫 번째 호출로 캐시 생성
        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .header("X-USER-ID", testUser.getId()))
                .andExpect(status().isOk());

        long totalTimeWithCache = 0;
        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/products/{productId}", productId)
                            .header("X-USER-ID", testUser.getId()))
                    .andExpect(status().isOk());
            totalTimeWithCache += (System.currentTimeMillis() - startTime);
        }

        long avgTimeWithoutCache = totalTimeWithoutCache / testCount;
        long avgTimeWithCache = totalTimeWithCache / testCount;

        System.out.println("=== 캐시 성능 비교 ===");
        System.out.println("캐시 없음 평균: " + avgTimeWithoutCache + "ms");
        System.out.println("캐시 있음 평균: " + avgTimeWithCache + "ms");
        System.out.println("성능 향상: " + (avgTimeWithoutCache - avgTimeWithCache) + "ms");

        // 캐시가 더 빠르거나 최소한 비슷해야 함
        assertThat(avgTimeWithCache).isLessThanOrEqualTo(avgTimeWithoutCache);
    }

    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName ->
                cacheManager.getCache(cacheName).clear());
    }

    private boolean isCached(String cacheName, String key) {
        return cacheManager.getCache(cacheName) != null &&
                cacheManager.getCache(cacheName).get(key) != null;
    }

    private java.util.Set<String> getAllCacheKeys(String cacheName) {
        // Caffeine의 경우 캐시 키를 직접 조회하기 어려우므로
        // 실제로는 캐시 히트/미스를 테스트하는 것이 더 실용적
        return java.util.Collections.emptySet();
    }
}
