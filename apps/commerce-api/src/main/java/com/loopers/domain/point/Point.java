package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.vo.Balance;
import com.loopers.domain.user.vo.UserId;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "point")
public class Point extends BaseEntity {

    @Embedded
    private UserId refUserId;
    @Embedded
    private Balance balance;

    public Point(){}
    public Point(UserId refUserId) {
        this.refUserId = refUserId;
    }
    public Point(UserId refUserId, Balance balance) {
        this.refUserId = refUserId;
        this.balance = balance;
    }

    public static Point from(String refUserId, Long balance) {
        return new Point(
            UserId.from(refUserId),
            Balance.from(balance)
        );
    }

    public static Point charge(Point point, Long amount){
        Balance plusedBalance = Balance.plus(point, amount);
        return new Point(
                point.getRefUserId(),
                plusedBalance
        );
    }

}
