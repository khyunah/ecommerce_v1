package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "point")
public class Point {
    @Id
    private Long refUserId;
    private int point;

    public Point(){}
    public Point(Long refUserId) {
        this.refUserId = refUserId;
    }
    public Point(Long refUserId, int point) {
        this.refUserId = refUserId;
        this.point = point;
    }

    public static void validatePoint(int point){
        if (point <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "0 이하의 정수로 포인트를 충전할 수 없습니다.");
        }
    }

    public static Point charge(Point point, int amount){
        validatePoint(point.getPoint());
        return new Point(
                point.getRefUserId(),
                point.getPoint() + amount
        );
    }


}
