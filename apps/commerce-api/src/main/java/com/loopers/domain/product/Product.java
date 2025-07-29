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
    @Column(name = "original_price", nullable = false)
    private Money originalPrice;

    @Embedded
    @Column(name = "selling_price", nullable = false)
    private Money sellingPrice;

    @Embedded
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    @Column(nullable = false)
    private Long refBrandId;

    public static Product from(String name, String description, BigDecimal sellingPrice, BigDecimal originalPrice, String saleStatus, Long refBrandId) {
        validate(name, sellingPrice, originalPrice);
        return new Product(
                name,
                description,
                Money.from(sellingPrice),
                Money.from(originalPrice),
                SaleStatus.from(saleStatus),
                refBrandId
        );
    }

    public static void validate(String name, BigDecimal sellingPrice, BigDecimal originalPrice){
        // 유효하지 않은 브랜드 ID 인 경우는 application 계층에서 하기

        // 원가보다 할인가가 높은 경우
        if(originalPrice.compareTo(sellingPrice) < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "원가보다 할인가가 높을 수 없습니다.");
        }
        // name이 null 인경우
        else if (name == null || name.isEmpty()){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 null이거나 빈 문자열일 수 없습니다.");
        }

    }
}
