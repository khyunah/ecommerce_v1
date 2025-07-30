package com.loopers.application.product;

import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;
    private final BrandService brandService;

    public ProductDetailInfo getDetail(Long productId) {
        Product product = productService.getDetail(productId);
        Brand brand = brandService.get(productId);
        return ProductDetailInfo.from(product, brand);
    }
}
