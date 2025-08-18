package com.loopers.domain.product;

import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.domain.product.vo.SaleStatus;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProductServiceTest {
    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getDetail_success() {
        // given
        Long productId = 1L;
        Product product = Product.from("상품1", "설명", BigDecimal.valueOf(20000L), BigDecimal.valueOf(30000L), "ON_SALE", 1L);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        Product result = productService.getDetail(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(product.getId());
        assertThat(result.getName()).isEqualTo("상품1");

        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않는 상품")
    void getDetail_notFound() {
        // given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getDetail(productId))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND)
                .hasMessage("상품 ID가 존재하지 않습니다.");

        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 존재 여부 확인")
    void existsById() {
        // given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        // when
        boolean result = productService.existsById(productId);

        // then
        assertThat(result).isTrue();
        verify(productRepository).existsById(productId);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 최신순 정렬")
    void getProducts_latest_success() {
        // given
        Long brandId = 1L;
        ProductSortType sortType = ProductSortType.LATEST;
        Pageable pageable = PageRequest.of(0, 10);

        List<ProductWithLikeCountDto> products = List.of(
                new ProductWithLikeCountDto(2L, "상품2", BigDecimal.valueOf(20000), BigDecimal.valueOf(18000), SaleStatus.ON_SALE, 1L, "브랜드1", 3L),
                new ProductWithLikeCountDto(1L, "상품1", BigDecimal.valueOf(10000), BigDecimal.valueOf(8000), SaleStatus.ON_SALE, 1L, "브랜드1", 5L)
        );
        Page<ProductWithLikeCountDto> expectedPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findProductsWithLikeCount(brandId, sortType, pageable)).thenReturn(expectedPage);

        // when
        Page<ProductWithLikeCountDto> result = productService.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        // 최신순이므로 상품2가 먼저
        assertThat(result.getContent().get(0).productName()).isEqualTo("상품2");
        assertThat(result.getContent().get(1).productName()).isEqualTo("상품1");

        verify(productRepository).findProductsWithLikeCount(brandId, sortType, pageable);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 좋아요 수순 정렬")
    void getProducts_likeCount_success() {
        // given
        Long brandId = 1L;
        ProductSortType sortType = ProductSortType.LIKE_COUNT;
        Pageable pageable = PageRequest.of(0, 10);

        // 좋아요 수가 많은 순으로 정렬된 상품 목록 (10개 > 7개 > 3개)
        List<ProductWithLikeCountDto> products = List.of(
                new ProductWithLikeCountDto(3L, "인기상품", BigDecimal.valueOf(30000), BigDecimal.valueOf(25000), SaleStatus.ON_SALE, 1L, "브랜드1", 10L),
                new ProductWithLikeCountDto(1L, "상품1", BigDecimal.valueOf(10000), BigDecimal.valueOf(8000), SaleStatus.ON_SALE, 1L, "브랜드1", 7L),
                new ProductWithLikeCountDto(2L, "상품2", BigDecimal.valueOf(20000), BigDecimal.valueOf(18000), SaleStatus.ON_SALE, 1L, "브랜드1", 3L)
        );
        Page<ProductWithLikeCountDto> expectedPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findProductsWithLikeCount(brandId, sortType, pageable)).thenReturn(expectedPage);

        // when
        Page<ProductWithLikeCountDto> result = productService.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        
        // 좋아요 수가 많은 순으로 정렬되어 있는지 확인
        List<ProductWithLikeCountDto> resultProducts = result.getContent();
        assertThat(resultProducts.get(0).likeCount()).isEqualTo(10L); // 첫 번째: 10개
        assertThat(resultProducts.get(0).productName()).isEqualTo("인기상품");
        
        assertThat(resultProducts.get(1).likeCount()).isEqualTo(7L);  // 두 번째: 7개
        assertThat(resultProducts.get(1).productName()).isEqualTo("상품1");
        
        assertThat(resultProducts.get(2).likeCount()).isEqualTo(3L);  // 세 번째: 3개
        assertThat(resultProducts.get(2).productName()).isEqualTo("상품2");

        verify(productRepository).findProductsWithLikeCount(brandId, sortType, pageable);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 브랜드 필터 없이 좋아요 수순 정렬")
    void getProducts_likeCount_noBrandFilter_success() {
        // given
        Long brandId = null; // 브랜드 필터 없음
        ProductSortType sortType = ProductSortType.LIKE_COUNT;
        Pageable pageable = PageRequest.of(0, 5);

        List<ProductWithLikeCountDto> products = List.of(
                new ProductWithLikeCountDto(5L, "전체1위상품", BigDecimal.valueOf(50000), BigDecimal.valueOf(45000), SaleStatus.ON_SALE, 2L, "브랜드2", 15L),
                new ProductWithLikeCountDto(3L, "전체2위상품", BigDecimal.valueOf(30000), BigDecimal.valueOf(25000), SaleStatus.ON_SALE, 1L, "브랜드1", 10L)
        );
        Page<ProductWithLikeCountDto> expectedPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findProductsWithLikeCount(brandId, sortType, pageable)).thenReturn(expectedPage);

        // when
        Page<ProductWithLikeCountDto> result = productService.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        
        // 전체 상품 중 좋아요 수가 많은 순으로 정렬 확인
        List<ProductWithLikeCountDto> resultProducts = result.getContent();
        assertThat(resultProducts.get(0).likeCount()).isEqualTo(15L);
        assertThat(resultProducts.get(0).brandName()).isEqualTo("브랜드2");
        
        assertThat(resultProducts.get(1).likeCount()).isEqualTo(10L);
        assertThat(resultProducts.get(1).brandName()).isEqualTo("브랜드1");

        verify(productRepository).findProductsWithLikeCount(brandId, sortType, pageable);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 빈 결과")
    void getProducts_emptyResult() {
        // given
        Long brandId = 999L; // 존재하지 않는 브랜드
        ProductSortType sortType = ProductSortType.LATEST;
        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductWithLikeCountDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findProductsWithLikeCount(brandId, sortType, pageable)).thenReturn(emptyPage);

        // when
        Page<ProductWithLikeCountDto> result = productService.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(productRepository).findProductsWithLikeCount(brandId, sortType, pageable);
    }
}
