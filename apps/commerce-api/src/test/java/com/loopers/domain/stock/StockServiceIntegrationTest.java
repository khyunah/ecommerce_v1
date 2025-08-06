package com.loopers.domain.stock;

import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class StockServiceIntegrationTest {
    private StockService stockSpyService;
    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        StockService realService = new StockService(stockRepository);
        stockSpyService = Mockito.spy(realService);
    }

    @Test
    void 재고가_저장된다(){
        // given
        // when
        Stock stock = stockSpyService.save(Stock.from(1L,100));

        // than
        assertThat(stock.getQuantity()).isEqualTo(100);
    }

    @Test
    void 재고가_업데이트된다(){
        // given
        Stock stock_ = Stock.from(1L,100);
        Stock stock = stockSpyService.save(stock_);

        // when
        Stock stock1 = stockSpyService.updateQuantity(stock,10);

        // than
        assertThat(stock1.getQuantity()).isEqualTo(90);
    }
}
