package com.loopers.application.product;

import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;
    private final BrandService brandService;

    // 최신 상품(3개월이내)만 캐싱
    @Cacheable(value = "productDetail",
            key = "'detail:' + #productId",
            condition = "@productService.isRecentProduct(#productId)")
    public ProductDetailInfo getDetail(Long productId) {
        Product product = productService.getDetail(productId);
        Brand brand = brandService.get(product.getRefBrandId());
        return ProductDetailInfo.from(product, brand);
    }

    public Page<ProductWithLikeCountDto> getProducts(Long brandId, ProductSortType sortType, Pageable pageable) {
        return productService.getProducts(brandId, sortType, pageable);
    }
}
