package com.loopers.application.product;

import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.domain.product.vo.SaleStatus;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private LikeRepository likeRepository;

    private Brand testBrand1;
    private Brand testBrand2;
    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();

        // 테스트 브랜드 생성
        testBrand1 = Brand.from("테스트브랜드1", "브랜드1 설명");
        testBrand1 = brandRepository.save(testBrand1);

        testBrand2 = Brand.from("테스트브랜드2", "브랜드2 설명");
        testBrand2 = brandRepository.save(testBrand2);

        // 테스트 상품 생성
        testProduct1 = Product.from(
                "상품1",
                "상품1 설명",
                BigDecimal.valueOf(20000),    // 할인가
                BigDecimal.valueOf(30000),    // 원가
                "ON_SALE",
                testBrand1.getId()
        );
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = Product.from(
                "상품2",
                "상품2 설명",
                BigDecimal.valueOf(40000),    // 할인가
                BigDecimal.valueOf(50000),    // 원가
                "ON_SALE",
                testBrand1.getId()
        );
        testProduct2 = productRepository.save(testProduct2);

        testProduct3 = Product.from(
                "상품3",
                "상품3 설명",
                BigDecimal.valueOf(20000),    // 할인가
                BigDecimal.valueOf(25000),    // 원가
                "SOLD_OUT",
                testBrand2.getId()
        );
        testProduct3 = productRepository.save(testProduct3);
    }

    @Test
    @DisplayName("상품 상세 조회 성공 - 실제 데이터베이스에서 상품, 브랜드 정보 조합")
    void getDetail_success() {
        // given
        Long productId = testProduct1.getId();

        // 좋아요 데이터 생성
        Like like1 = Like.from(1L, productId);
        Like like2 = Like.from(2L, productId);

        likeRepository.save(like1);
        likeRepository.save(like2);

        // 상품의 좋아요 수 업데이트
        Product.increaseLikeCount(testProduct1);
        Product.increaseLikeCount(testProduct1);
//        productRepository.save(testProduct1);

        // when
        ProductDetailInfo result = productFacade.getDetail(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testProduct1.getId());
        assertThat(result.name()).isEqualTo("상품1");
        assertThat(result.description()).isEqualTo("상품1 설명");
        assertThat(result.originalPrice()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(result.sellingPrice()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(result.saleStatus()).isEqualTo(SaleStatus.ON_SALE.name());
        assertThat(result.likeCount()).isEqualTo(2L);
        assertThat(result.brandId()).isEqualTo(testBrand1.getId());
        assertThat(result.brandName()).isEqualTo("테스트브랜드1");
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 브랜드 필터링 적용")
    void getProducts_withBrandFilter_success() {
        // given
        Long brandId = testBrand1.getId();
        ProductSortType sortType = ProductSortType.LATEST;
        Pageable pageable = PageRequest.of(0, 10);

        // 좋아요 데이터 생성 (상품2에 더 많은 좋아요)
        createLikesForProduct(testProduct1.getId(), 3);
        createLikesForProduct(testProduct2.getId(), 5);
        createLikesForProduct(testProduct3.getId(), 1);

        // when
        Page<ProductWithLikeCountDto> result = productFacade.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // testBrand1에 속한 상품 2개

        List<ProductWithLikeCountDto> products = result.getContent();
        assertThat(products).extracting(ProductWithLikeCountDto::productId)
                .containsExactlyInAnyOrder(testProduct1.getId(), testProduct2.getId());

        assertThat(products).extracting(ProductWithLikeCountDto::brandName)
                .allMatch(brandName -> brandName.equals("테스트브랜드1"));
    }

    @Test
    @DisplayName("상품 목록 조회 - 좋아요 수 정렬")
    void getProducts_sortByLikeCount() {
        // given
        Long brandId = null; // 모든 브랜드
        ProductSortType sortType = ProductSortType.LIKE_COUNT;
        Pageable pageable = PageRequest.of(0, 10);

        // 좋아요 데이터 생성 (상품2 > 상품1 > 상품3 순)
        createLikesForProduct(testProduct1.getId(), 3);
        createLikesForProduct(testProduct2.getId(), 7);
        createLikesForProduct(testProduct3.getId(), 1);

        // when
        Page<ProductWithLikeCountDto> result = productFacade.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        List<ProductWithLikeCountDto> products = result.getContent();

        // 좋아요 수 내림차순 정렬 확인
        assertThat(products.get(0).likeCount()).isEqualTo(7L); // 상품2
        assertThat(products.get(1).likeCount()).isEqualTo(3L); // 상품1
        assertThat(products.get(2).likeCount()).isEqualTo(1L); // 상품3

        assertThat(products.get(0).productId()).isEqualTo(testProduct2.getId());
        assertThat(products.get(1).productId()).isEqualTo(testProduct1.getId());
        assertThat(products.get(2).productId()).isEqualTo(testProduct3.getId());
    }

    @Test
    @DisplayName("상품 목록 조회 - 브랜드 필터 없이 모든 상품 조회")
    void getProducts_noBrandFilter() {
        // given
        Long brandId = null;
        ProductSortType sortType = ProductSortType.LATEST;
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<ProductWithLikeCountDto> result = productFacade.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3); // 모든 상품
        assertThat(result.getTotalElements()).isEqualTo(3);

        List<ProductWithLikeCountDto> products = result.getContent();
        assertThat(products).extracting(ProductWithLikeCountDto::productId)
                .containsExactlyInAnyOrder(
                        testProduct1.getId(),
                        testProduct2.getId(),
                        testProduct3.getId()
                );
    }

    @Test
    @DisplayName("상품 목록 조회 - 페이징 처리")
    void getProducts_withPaging() {
        // given
        Long brandId = null;
        ProductSortType sortType = ProductSortType.LATEST;
        Pageable pageable = PageRequest.of(0, 2); // 페이지 크기 2

        // when
        Page<ProductWithLikeCountDto> result = productFacade.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // 페이지 크기만큼
        assertThat(result.getTotalElements()).isEqualTo(3); // 전체 요소 수
        assertThat(result.getTotalPages()).isEqualTo(2); // 전체 페이지 수
        assertThat(result.hasNext()).isTrue(); // 다음 페이지 존재
    }

    @Test
    @DisplayName("상품 목록 조회 - 존재하지 않는 브랜드로 필터링")
    void getProducts_withNonExistentBrand() {
        // given
        Long nonExistentBrandId = 999L;
        ProductSortType sortType = ProductSortType.LATEST;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductWithLikeCountDto> result = productFacade.getProducts(nonExistentBrandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 상세 조회 - 좋아요가 없는 상품")
    void getDetail_productWithNoLikes() {
        // given
        Long productId = testProduct3.getId();

        // when
        ProductDetailInfo result = productFacade.getDetail(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testProduct3.getId());
        assertThat(result.likeCount()).isEqualTo(0L);
        assertThat(result.brandId()).isEqualTo(testBrand2.getId());
        assertThat(result.brandName()).isEqualTo("테스트브랜드2");
    }

    /**
     * 특정 상품에 대해 지정된 수만큼 좋아요를 생성하는 헬퍼 메서드
     */
    private void createLikesForProduct(Long productId, int likeCount) {
        for (int i = 1; i <= likeCount; i++) {
            Like like = Like.from((long) i, productId);
            likeRepository.save(like);
        }

        // 상품의 좋아요 수 업데이트
        Product product = productRepository.findById(productId).orElseThrow();
        for (int i = 1; i <= likeCount; i++) {
            Product.increaseLikeCount(product);
        }
        productRepository.save(product);
    }
}
