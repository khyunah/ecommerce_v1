package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.vo.Money;
import com.loopers.domain.product.vo.SaleStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "original_price", nullable = false))
    private Money originalPrice;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "selling_price", nullable = false))
    private Money sellingPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    @Column(nullable = false)
    private Long refBrandId;

    public static Product from(String name, String description, BigDecimal sellingPrice, BigDecimal originalPrice, String saleStatus, Long refBrandId) {
        validateName(name);
        validateOriginalPriceAndSellingPrice(originalPrice,sellingPrice);
        return new Product(
                name,
                description,
                Money.from(originalPrice),
                Money.from(sellingPrice),
                SaleStatus.from(saleStatus),
                refBrandId
        );
    }

    public static void validateName(String name){
        if (name == null || name.isEmpty()){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    public static void validateOriginalPriceAndSellingPrice(BigDecimal originalPrice, BigDecimal sellingPrice){
        if(originalPrice.compareTo(sellingPrice) < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "원가보다 할인가가 높을 수 없습니다.");
        }
    }
}
