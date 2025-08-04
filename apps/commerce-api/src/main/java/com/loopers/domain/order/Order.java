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

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @JoinColumn(name = "order_id")
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(Long refUserId, List<OrderItem> orderItems) {
        if(orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 아이템은 하나 이상이어야 합니다.");
        }
        return new Order(refUserId, OrderStatus.ORDERED, orderItems);
    }
}
