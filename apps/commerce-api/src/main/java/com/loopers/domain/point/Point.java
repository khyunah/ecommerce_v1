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

    @Column(nullable = false)
    private Long refUserId;
    @Embedded
    private Balance balance;

    public Point(){}
    public Point(Long refUserId) {
        this.refUserId = refUserId;
    }
    public Point(Long refUserId, Balance balance) {
        this.refUserId = refUserId;
        this.balance = balance;
    }

    public static Point from(Long refUserId, Long balance) {
        return new Point(
                refUserId,
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

    public static Point minus(Point point, Long amount){

        point.balance = Balance.minus(point, amount);
        return point;
    }

}
