package com.loopers.domain.stock;

import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findByRefProductId(Long refProductId);
    Stock save(Stock stock);

}
