package com.loopers.domain.point.vo;

import com.loopers.domain.point.Point;
import com.loopers.domain.user.vo.Email;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Embeddable
public class Balance {

    @Column(name = "balance")
    private Long value;

    public Balance() {}

    public Balance(Long value) {
        this.value = value;
    }

    public static Balance from(Long value) {
        validate(value);
        return new Balance(value);
    }

    public static void validate(Long balance){
        if (balance <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "0 이하의 정수로 포인트를 충전할 수 없습니다.");
        }
    }

    public static Balance plus(Point point, Long amount){
        validate(amount);
        return new Balance(amount + point.getBalance().getValue());
    }
}
