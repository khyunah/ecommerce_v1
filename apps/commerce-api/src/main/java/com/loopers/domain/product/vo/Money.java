package com.loopers.domain.product.vo;

import com.loopers.domain.user.vo.Email;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@ToString
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Money {

    private BigDecimal value;

    public static Money from(BigDecimal value) {
        validate(value);
        return new Money(value);
    }

    public static void validate(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 비어있지 않으며, 음수일 수 없습니다.");
        }
    }

    public static BigDecimal calculateDiscountRate(BigDecimal originalPrice, BigDecimal sellingPrice) {
        BigDecimal discount = originalPrice.subtract(sellingPrice);
        return discount
                .divide(originalPrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
