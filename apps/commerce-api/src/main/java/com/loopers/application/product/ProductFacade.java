package com.loopers.application.product;

import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.infrastructure.event.EventPublishService;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;
    private final BrandService brandService;
    
    @Autowired(required = false) // 선택적 의존성 - 테스트에서는 null 가능
    private EventPublishService eventPublishService;

    // 기존 캐시된 메서드 (이벤트 발행 없음)
    @Cacheable(value = "productDetail",
            key = "'detail:' + #productId",
            condition = "@productService.isRecentProduct(#productId)")
    public ProductDetailInfo getDetail(Long productId) {
        Product product = productService.getDetail(productId);
        Brand brand = brandService.get(product.getRefBrandId());
        return ProductDetailInfo.from(product, brand);
    }

    /**
     * 상품 상세 조회 (이벤트 발행 포함)
     * Controller에서 사용자 정보가 있을 때 호출
     */
    public ProductDetailInfo getDetailWithEvent(Long productId, Long userId, String sessionId) {
        Product product = productService.getDetail(productId);
        Brand brand = brandService.get(product.getRefBrandId());
        
        // 상품 조회 이벤트 발행 (EventPublishService가 있을 때만)
        publishProductViewedEventIfAvailable(productId, userId, sessionId);
        
        return ProductDetailInfo.from(product, brand);
    }

    public Page<ProductWithLikeCountDto> getProducts(Long brandId, ProductSortType sortType, Pageable pageable) {
        return productService.getProducts(brandId, sortType, pageable);
    }
    
    /**
     * EventPublishService가 있을 때만 이벤트 발행 (Null-Safe)
     */
    private void publishProductViewedEventIfAvailable(Long productId, Long userId, String sessionId) {
        if (eventPublishService != null) {
            eventPublishService.publishProductViewed(productId, userId, sessionId);
        }
    }
}
