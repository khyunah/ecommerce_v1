package com.loopers.domain.product;

import com.loopers.domain.product.vo.Money;
import com.loopers.domain.product.vo.SaleStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @DisplayName("원가가 할인가보다 작은 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateProduct_whenOriginalPriceIsLessThanSellingPrice() {
        // given
        BigDecimal originalPrice = new BigDecimal("40000");
        BigDecimal sellingPrice = new BigDecimal("50000");

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Product.validateOriginalPriceAndSellingPrice(originalPrice,sellingPrice);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("원가보다 할인가가 높을 수 없습니다.");
    }

    @DisplayName("상품명이 null이거나 빈 문자열일경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateProduct_whenNameIsNull_orEmpty() {
        // given
        BigDecimal originalPrice = new BigDecimal("40000");
        BigDecimal sellingPrice = new BigDecimal("50000");

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Product.validateOriginalPriceAndSellingPrice(originalPrice,sellingPrice);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("상품명은 null이거나 빈 문자열일 수 없습니다.");
    }

    @DisplayName("Money의 value가 null인 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateMoney_whenValueIsNull() {
        // given
        BigDecimal originalPrice = null;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Money.validate(originalPrice);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("가격은 null일 수 없습니다.");
    }

    @DisplayName("Money의 value가 음수인 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateMoney_whenValueIsNegative() {
        // given
        BigDecimal originalPrice = new BigDecimal("-50000");

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Money.validate(originalPrice);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("가격은 음수일 수 없습니다.");
    }

    @DisplayName("SaleStatus는 null이거나 빈 문자열일 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateProduct_whenSaleStatusIsNullOrBlank() {
        // given
        String saleStatus = "";

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            SaleStatus.validate(saleStatus);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("SaleStatus는 null이거나 빈 문자열일 수 없습니다.");
    }

    @DisplayName("SaleStatus에 정의된 값이 아닐 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateProduct_whenSaleStatusIsInvalid() {
        // given
        String saleStatus = "ON_SALE_";

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            SaleStatus.validate(saleStatus);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("사용 가능한 SaleStatus가 아닙니다.");
    }

}
