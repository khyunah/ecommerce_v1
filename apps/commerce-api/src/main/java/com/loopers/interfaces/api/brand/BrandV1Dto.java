package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.out.BrandInfo;

public class BrandV1Dto {
    public record BrandInfoResponse(
            Long id,
            String name,
            String description
    ) {
        public static BrandInfoResponse from(BrandInfo brandInfo) {
            return new BrandInfoResponse(
                    brandInfo.id(),
                    brandInfo.name(),
                    brandInfo.description()
            );
        }
    }

}
