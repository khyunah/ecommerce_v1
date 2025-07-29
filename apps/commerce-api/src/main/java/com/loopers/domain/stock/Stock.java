package com.loopers.domain.stock;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stock")
@NoArgsConstructor
@AllArgsConstructor
public class Stock extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long refProductId;
    @Column(nullable = false)
    private int quantity;

    public static Stock from(Long refProductId, int quantity) {
        validateRefProductId(refProductId);
        validateQuantity(quantity);
        return new Stock(
                refProductId,
                quantity
        );
    }

    private static void validateRefProductId(Long refProductId) {
        if(refProductId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 null일 수 없습니다.");
        }
    }

    private static void validateQuantity(int quantity) {
        if(quantity < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "재고 수량은 음수일 수 없습니다.");
        }
    }
}
