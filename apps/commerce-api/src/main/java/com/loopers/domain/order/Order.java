package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "`order`")
public class Order extends BaseEntity {
    @Column(nullable = false)
    private Long refUserId;

    @Column(nullable = false, unique = true)
    private String orderSeq;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(Long refUserId, String orderSeq, List<OrderItem> orderItems) {
        return new Order(refUserId, orderSeq, OrderStatus.ORDERED, orderItems);
    }
}
