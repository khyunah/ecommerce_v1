package com.loopers.domain.product;

import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    boolean existsById(Long id);
    Product save(Product product);
    Page<ProductWithLikeCountDto> findProductsWithLikeCount(Long brandId, ProductSortType sortType, Pageable pageable);
    void deleteAll();
}
