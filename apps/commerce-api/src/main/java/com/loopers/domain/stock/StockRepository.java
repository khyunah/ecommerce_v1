package com.loopers.domain.stock;

import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findByRefProductIdWithLock(Long refProductId);
    Optional<Stock> findByRefProductId(Long refProductId);
    Stock save(Stock stock);
    Optional<Stock> findById(Long stockId);
}
