package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandRepository {
    Optional<Brand> findById(Long brandId);
    Brand save(Brand testBrand1);
    void deleteAll();
}
