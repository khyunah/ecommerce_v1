package com.loopers.application.product;

import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.domain.product.vo.SaleStatus;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProductFacadeTest {
    private ProductService productService;
    private BrandService brandService;
    private ProductFacade productFacade;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        brandService = mock(BrandService.class);
        productFacade = new ProductFacade(productService, brandService);
    }

    @Test
    @DisplayName("상품 상세 조회 성공 - 상품, 좋아요 수, 브랜드 정보 조합")
    void getDetail_success() {
        // given
        Long productId = 1L;
        Product product = Product.from("상품1","설명", BigDecimal.valueOf(20000L), BigDecimal.valueOf(30000L),"ON_SALE", 1L);
        Brand brand = mock(Brand.class);
        Long likeCount = 42L;

        when(productService.getDetail(productId)).thenReturn(product);
        when(brandService.get(productId)).thenReturn(brand);
        when(productService.getDetail(productId).getLikeCount()).thenReturn(likeCount);

        // when
        ProductDetailInfo result = productFacade.getDetail(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.likeCount()).isEqualTo(likeCount);
        assertThat(result.id()).isEqualTo(product.getId());
        assertThat(result.brandId()).isEqualTo(brand.getId());

        // 모든 서비스가 호출되었는지 확인 (Facade의 주요 역할)
        verify(productService).getDetail(productId);
        verify(brandService).get(productId);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - ProductService에 올바른 파라미터 전달")
    void getProducts_success() {
        // given
        Long brandId = 1L;
        ProductSortType sortType = ProductSortType.LIKE_COUNT;
        Pageable pageable = PageRequest.of(0, 10);

        List<ProductWithLikeCountDto> products = List.of(
                new ProductWithLikeCountDto(1L, "상품1", BigDecimal.valueOf(10000), BigDecimal.valueOf(8000), SaleStatus.ON_SALE, 1L, "브랜드1", 5L)
        );
        Page<ProductWithLikeCountDto> expectedPage = new PageImpl<>(products, pageable, products.size());

        when(productService.getProducts(brandId, sortType, pageable)).thenReturn(expectedPage);

        // when
        Page<ProductWithLikeCountDto> result = productFacade.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).productId()).isEqualTo(1L);

        // Facade가 ProductService에 올바른 파라미터를 전달했는지 확인
        verify(productService).getProducts(brandId, sortType, pageable);
        verifyNoInteractions(brandService); // 다른 서비스는 호출되지 않아야 함
    }

    @Test
    @DisplayName("상품 목록 조회 - 브랜드 필터 없이 호출")
    void getProducts_noBrandFilter() {
        // given
        Long brandId = null;
        ProductSortType sortType = ProductSortType.LATEST;
        Pageable pageable = PageRequest.of(0, 5);

        Page<ProductWithLikeCountDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(productService.getProducts(brandId, sortType, pageable)).thenReturn(emptyPage);

        // when
        Page<ProductWithLikeCountDto> result = productFacade.getProducts(brandId, sortType, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        // null 브랜드ID도 올바르게 전달되었는지 확인
        verify(productService).getProducts(null, sortType, pageable);
    }
}
