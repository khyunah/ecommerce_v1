package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class StockRepositoryImpl implements StockRepository {

    private final StockJpaRepository jpaStockRepository;

    @Override
    public Optional<Stock> findByRefProductId(Long refProductId) {
        return jpaStockRepository.findByRefProductId(refProductId);
    }

    @Override
    public Stock save(Stock stock) {
        return jpaStockRepository.save(stock);
    }
}
