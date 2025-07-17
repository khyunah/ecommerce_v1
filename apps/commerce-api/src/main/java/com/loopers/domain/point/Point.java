package com.loopers.domain.point;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "point")
public class Point {
    @Id
    private String userId;
    private int point;

    public Point(){}
    public Point(String userId) {
        this.userId =  userId;
    }
    public Point(String userId, int point) {
        this.userId =  userId;
        this.point = point;
    }

}
