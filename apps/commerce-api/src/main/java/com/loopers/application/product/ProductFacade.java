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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;
    private final BrandService brandService;
    private final LikeService likeService;

    public ProductDetailInfo getDetail(Long productId) {
        Product product = productService.getDetail(productId);
        Long likeCount = likeService.countLikeByProductId(productId);
        Brand brand = brandService.get(productId);
        return ProductDetailInfo.from(product, likeCount, brand);
    }

    public Page<ProductWithLikeCountDto> getProducts(Long brandId, ProductSortType sortType, Pageable pageable) {
        return productService.getProducts(brandId, sortType, pageable);
    }
}
