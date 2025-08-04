package com.loopers.application.product;

import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProductFacadeTest {
    private ProductService productService;
    private BrandService brandService;
    private LikeService likeService;
    private ProductFacade productFacade;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        brandService = mock(BrandService.class);
        likeService = mock(LikeService.class);
        productFacade = new ProductFacade(productService, brandService, likeService);
    }

    @Test
    @DisplayName("상품 상세 조회 성공 - 상품, 좋아요 수, 브랜드 정보 반환")
    void getDetail_success() {
        // given
        Long productId = 1L;
        Product product = Product.from("상품1","설명", BigDecimal.valueOf(20000L), BigDecimal.valueOf(30000L),"ON_SALE", 1L);
        Brand brand = mock(Brand.class);
        Long likeCount = 42L;

        when(productService.getDetail(productId)).thenReturn(product);
        when(brandService.get(productId)).thenReturn(brand);
        when(likeService.countLikeByProductId(productId)).thenReturn(likeCount);

        // when
        ProductDetailInfo result = productFacade.getDetail(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.likeCount()).isEqualTo(likeCount);
        assertThat(result.id()).isEqualTo(product.getId());
        assertThat(result.brandId()).isEqualTo(brand.getId());

        verify(productService).getDetail(productId);
        verify(brandService).get(productId);
        verify(likeService).countLikeByProductId(productId);
    }
}
