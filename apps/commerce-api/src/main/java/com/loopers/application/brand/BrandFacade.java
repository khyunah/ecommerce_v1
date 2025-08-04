package com.loopers.application.brand;

import com.loopers.application.brand.out.BrandInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BrandFacade {
    private final BrandService brandService;

    public BrandInfo get(Long brandId) {
        Brand brand = brandService.get(brandId);
        return BrandInfo.from(brand);
    }
}
