package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.out.ProductDetailInfo;
import com.loopers.domain.product.vo.ProductSortType;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AuthenticatedUserIdProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductV1Controller {
    private final ProductFacade productFacade;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductV1Dto.ProductInfoResponse>> get(@PathVariable Long productId, HttpServletRequest headers){
        Long userId = AuthenticatedUserIdProvider.getUserId(headers);
        ProductDetailInfo productDetailInfo = productFacade.getDetail(productId);
        ProductV1Dto.ProductInfoResponse response = ProductV1Dto.ProductInfoResponse.from(productDetailInfo);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductV1Dto.ProductListResponse>> getProducts(
            @RequestParam(required = false) Long brandId, 
            @RequestParam(defaultValue = "LATEST") String sortType,
            HttpServletRequest headers, 
            @PageableDefault Pageable pageable) {
        Long userId = AuthenticatedUserIdProvider.getUserId(headers);
        ProductSortType productSortType = ProductSortType.fromString(sortType);
        Page<ProductWithLikeCountDto> productPage = productFacade.getProducts(brandId, productSortType, pageable);
        ProductV1Dto.ProductListResponse response = ProductV1Dto.ProductListResponse.from(productPage);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }
}
