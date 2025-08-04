package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.out.BrandInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/brands")
public class BrandV1Controller {

    private final BrandFacade brandFacade;

    @GetMapping("/{brandId}")
    public ResponseEntity<BrandV1Dto.BrandInfoResponse> get(@PathVariable Long brandId){
        BrandInfo brandInfo = brandFacade.get(brandId);
        BrandV1Dto.BrandInfoResponse response = BrandV1Dto.BrandInfoResponse.from(brandInfo);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
