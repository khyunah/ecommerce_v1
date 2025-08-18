package com.loopers.domain.product;

import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Component
public class ProductService {
    public final ProductRepository productRepository;

    @Cacheable(value = "productDetail", key = "#productId")
    public Product getDetail(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 ID가 존재하지 않습니다."));
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    // 목록 조회가 많을 것으로 예상되는 기본 목록 조회 3페이지 까지 캐싱
    @Cacheable(value = "productList",
            key = "'latest:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize",
            condition = "#brandId == null && #sortType.name() == 'LATEST' && #pageable.pageNumber <= 2")
    public Page<ProductWithLikeCountDto> getProducts(Long brandId, ProductSortType sortType, Pageable pageable) {
        return productRepository.findProductsWithLikeCount(brandId, sortType, pageable);
    }

    public void increaseLikeCount(Product product){
        Product.increaseLikeCount(product);
    }

    public void decreaseLikeCount(Product product){
        Product.decreaseLikeCount(product);
    }

    public boolean isRecentProduct(Long productId) {  // ✅ boolean 리턴
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;

        return product.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(3));
    }
}
