package com.loopers.domain.stock;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockTest {

    @DisplayName("상품 ID가 null일 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateProduct_whenProductIdIsNull() {
        // given
        Long refProductId = null;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Stock.validateRefProductId(refProductId);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("상품 ID는 null일 수 없습니다.");
    }

    @DisplayName("상품 ID가 음수일 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateProduct_whenProductIdIsNegative() {
        // given
        Long refProductId = -1L;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Stock.validateRefProductId(refProductId);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("상품 ID는 음수일 수 없습니다.");
    }

    @DisplayName("재고 수량이 음수일 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateProduct_whenStockQuantityIsNegative() {
        // given
        int quantity = -1;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Stock.validateQuantity(quantity);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("재고 수량은 음수일 수 없습니다.");
    }
}
