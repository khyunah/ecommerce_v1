package com.loopers.domain.stock;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockService {
    private final StockRepository stockRepository;

    public Stock getByRefProductId(Long productId) {
        return stockRepository.findByRefProductId(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "재고가 존재하지 않습니다."));
    }

    public Stock save(Stock stock) {
        return stockRepository.save(stock);
    }

}
