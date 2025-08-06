package com.loopers.domain.stock;

import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import com.loopers.domain.point.vo.Balance;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @DisplayName("재고가 존재하지 않을 경우 404 Not Found 에러가 발생한다.")
    @Test
    void should_throw_not_found_exception_when_stock_does_not_exist(){
        // given
        Long stockId = 1L;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            stockSpyService.getByRefProductIdWithLock(stockId);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("재고가 존재하지 않습니다.");
    }

    @DisplayName("재고가 부족할 경우 IllegalArgumentException 에러가 발생한다.")
    @Test
    void should_throw_illegal_argument_exception_when_stock_is_insufficient(){
        // given
        Stock stock_ = Stock.from(1L,10);
        Stock stock = stockSpyService.save(stock_);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            stockSpyService.updateQuantity(stock,20);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("재고가 부족합니다.");
    }
}
