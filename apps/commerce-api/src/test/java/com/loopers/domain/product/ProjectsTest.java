package com.loopers.domain.product;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.SaleStatus;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductsTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("브랜드 ID 필터링 적용하여 상품 목록을 조회한다.")
    @Test
    void getProducts_shouldReturnPageOfProducts_whenBrandIdIsProvided() {
        // given
        Long brandId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        ProductWithLikeCountDto dto1 = new ProductWithLikeCountDto(
                101L, "상품1", new BigDecimal("10000"), new BigDecimal("9000"),
                SaleStatus.ON_SALE, brandId, "브랜드1", 5L);
        ProductWithLikeCountDto dto2 = new ProductWithLikeCountDto(
                102L, "상품2", new BigDecimal("15000"), new BigDecimal("12000"),
                SaleStatus.ON_SALE, brandId, "브랜드1", 3L);

        Page<ProductWithLikeCountDto> page = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        when(productRepository.findProductsWithLikeCount(eq(brandId), eq(pageable))).thenReturn(page);

        // when
        Page<ProductWithLikeCountDto> result = productService.getProducts(brandId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getProductName()).isEqualTo("상품1");
        assertThat(result.getContent().get(0).getLikeCount()).isEqualTo(5L);
        assertThat(result.getContent().get(1).getProductName()).isEqualTo("상품2");
    }

    @DisplayName("브랜드 ID 없이 전체 상품을 조회해온다.")
    @Test
    void getProducts_shouldReturnPageOfProducts_whenBrandIdIsNull() {
        Pageable pageable = PageRequest.of(0, 10);

        ProductWithLikeCountDto dto = new ProductWithLikeCountDto(
                201L, "상품A", new BigDecimal("20000"), new BigDecimal("18000"),
                SaleStatus.ON_SALE, 2L, "브랜드2", 7L);

        Page<ProductWithLikeCountDto> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(productRepository.findProductsWithLikeCount(eq(null), eq(pageable))).thenReturn(page);

        Page<ProductWithLikeCountDto> result = productService.getProducts(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBrandName()).isEqualTo("브랜드2");
    }
}
