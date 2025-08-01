package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.vo.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "orderItem")
public class OrderItem extends BaseEntity {
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String productName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "selling_price", nullable = false))
    private Money sellingPrice;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "original_price", nullable = false))
    private Money originalPrice;

    public static OrderItem create(Long productId, int quantity, String productName, Money sellingPrice, Money originalPrice) {
        if(quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        return new OrderItem(productId, quantity, productName, sellingPrice, originalPrice);
    }
}
