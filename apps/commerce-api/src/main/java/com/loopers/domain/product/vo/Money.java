package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

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
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 null일 수 없습니다.");

        } else if(value.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 음수일 수 없습니다.");
        }
    }

}
