package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.out.ProductDetailInfo;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ProductV1Dto.ProductInfoResponse> get(@PathVariable Long productId, @RequestHeader Map<String, String> headers){
        if( null == headers.get("X-USER-ID") && "".equals(headers.get("X-USER-ID"))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        ProductDetailInfo productDetailInfo = productFacade.getDetail(productId);
        ProductV1Dto.ProductInfoResponse response = ProductV1Dto.ProductInfoResponse.from(productDetailInfo);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
