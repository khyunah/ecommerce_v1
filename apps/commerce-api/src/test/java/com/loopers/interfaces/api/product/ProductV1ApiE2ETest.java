package com.loopers.interfaces.api.product;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
class ProductV1ApiE2ETest {

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

    private Brand testBrand;
    private User testUser1, testUser2, testUser3;
    private Product product1, product2, product3;

    @BeforeEach
    void setUp() throws InterruptedException {
        // 테스트 브랜드 생성
        testBrand = Brand.from("테스트브랜드", "테스트 브랜드 설명");
        testBrand = brandJpaRepository.save(testBrand);

        // 테스트 유저 생성
        testUser1 = User.from("user1", "user1@test.com", "1990-01-01", "M");
        testUser2 = User.from("user2", "user2@test.com", "1991-01-01", "F");
        testUser3 = User.from("user3", "user3@test.com", "1992-01-01", "M");
        testUser1 = userJpaRepository.save(testUser1);
        testUser2 = userJpaRepository.save(testUser2);
        testUser3 = userJpaRepository.save(testUser3);

        // 테스트 상품 생성 (생성 시간을 다르게 하기 위해 순서대로, 시간 간격 추가)
        product1 = Product.from("상품1", "설명1", BigDecimal.valueOf(10000), BigDecimal.valueOf(18000), "ON_SALE", testBrand.getId());
        product1 = productJpaRepository.save(product1);
        Thread.sleep(10); // 10ms 대기

        product2 = Product.from("상품2", "설명2", BigDecimal.valueOf(20000), BigDecimal.valueOf(28000), "ON_SALE", testBrand.getId());
        product2 = productJpaRepository.save(product2);
        Thread.sleep(10); // 10ms 대기

        product3 = Product.from("인기상품", "설명3", BigDecimal.valueOf(30000), BigDecimal.valueOf(35000), "ON_SALE", testBrand.getId());
        product3 = productJpaRepository.save(product3);

        // 좋아요 데이터 설정 (product3이 가장 많은 좋아요)
        // product3: 3개의 좋아요
        likeJpaRepository.save(Like.from(testUser1.getId(), product3.getId()));
        Product.increaseLikeCount(product3);
        likeJpaRepository.save(Like.from(testUser2.getId(), product3.getId()));
        Product.increaseLikeCount(product3);
        likeJpaRepository.save(Like.from(testUser3.getId(), product3.getId()));
        Product.increaseLikeCount(product3);

        // product1: 1개의 좋아요
        likeJpaRepository.save(Like.from(testUser1.getId(), product1.getId()));
        Product.increaseLikeCount(product1);

        // product2: 좋아요 없음
    }

    @Test
    @DisplayName("상품 목록 조회 - 최신순 정렬 (기본값)")
    void getProducts_defaultLatest() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                // 최신순이므로 가장 마지막에 생성된 상품이 첫 번째
                .andExpect(jsonPath("$.data.content[0].productName").value("인기상품"))
                .andExpect(jsonPath("$.data.content[1].productName").value("상품2"))
                .andExpect(jsonPath("$.data.content[2].productName").value("상품1"));
    }

    @Test
    @DisplayName("상품 목록 조회 - 최신순 정렬 (명시적 지정)")
    void getProducts_explicitLatest() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .param("sortType", "LATEST")
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].productName").value("인기상품"))
                .andExpect(jsonPath("$.data.content[1].productName").value("상품2"))
                .andExpect(jsonPath("$.data.content[2].productName").value("상품1"));
    }

    @Test
    @DisplayName("상품 목록 조회 - 좋아요 수순 정렬")
    void getProducts_likeCountOrder() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .param("sortType", "LIKE_COUNT")
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                // 좋아요 수순이므로 product3(3개), product1(1개), product2(0개) 순서
                .andExpect(jsonPath("$.data.content[0].productName").value("인기상품"))
                .andExpect(jsonPath("$.data.content[0].likeCount").value(3))
                .andExpect(jsonPath("$.data.content[1].productName").value("상품1"))
                .andExpect(jsonPath("$.data.content[1].likeCount").value(1))
                .andExpect(jsonPath("$.data.content[2].productName").value("상품2"))
                .andExpect(jsonPath("$.data.content[2].likeCount").value(0));
    }

    @Test
    @DisplayName("상품 목록 조회 - 대소문자 무관한 정렬 타입")
    void getProducts_caseInsensitiveSortType() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .param("sortType", "like_count") // 소문자
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].productName").value("인기상품"))
                .andExpect(jsonPath("$.data.content[0].likeCount").value(3));
    }

    @Test
    @DisplayName("상품 목록 조회 - 브랜드 필터 없이 좋아요 수순")
    void getProducts_noBrandFilter_likeCountOrder() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("sortType", "LIKE_COUNT")
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].productName").value("인기상품"))
                .andExpect(jsonPath("$.data.content[0].likeCount").value(3));
    }

    @Test
    @DisplayName("상품 목록 조회 - 잘못된 정렬 타입으로 400 에러")
    void getProducts_invalidSortType() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .param("sortType", "INVALID_TYPE")
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.meta.result").value("FAIL"))
                .andExpect(jsonPath("$.meta.message").value("유효하지 않은 정렬 타입입니다. (LATEST, LIKE_COUNT 중 하나를 입력해주세요)"));
    }

    @Test
    @DisplayName("상품 목록 조회 - 순서 확인용 디버깅 테스트")
    void getProducts_debug() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .param("sortType", "LATEST")
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andDo(result -> {
                    // 실제 응답을 출력해서 순서 확인
                    System.out.println("=== DEBUG: 실제 응답 ===");
                    System.out.println(result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("상품 목록 조회 - 페이징 테스트")
    void getProducts_withPaging() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("brandId", testBrand.getId().toString())
                        .param("sortType", "LIKE_COUNT")
                        .param("page", "0")
                        .param("size", "2")
                        .header("X-USER-ID", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.page").value(0));
    }
}
