package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class StockRepositoryImpl implements StockRepository {

    private final StockJpaRepository stockJpaRepository;

    @Override
    public Optional<Stock> findByRefProductIdWithLock(Long refProductId) {
        return stockJpaRepository.findByRefProductIdWithLock(refProductId);
    }

    @Override
    public Optional<Stock> findByRefProductId(Long refProductId) {
        return stockJpaRepository.findByRefProductId(refProductId);
    }

    @Override
    public Stock save(Stock stock) {
        return stockJpaRepository.save(stock);
    }

    @Override
    public Optional<Stock> findById(Long stockId) {
        return stockJpaRepository.findById(stockId);
    }
}
