package com.loopers.domain.product;

import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductService {
    public final ProductRepository productRepository;

    public Product getDetail(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다."));
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    public Page<ProductWithLikeCountDto> getProducts(Long brandId, Pageable pageable) {
        return productRepository.findProductsWithLikeCount(brandId, pageable);
    }
}
