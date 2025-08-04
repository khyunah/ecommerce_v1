package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.vo.Money;
import com.loopers.support.error.CoreException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "coupon")
public class Coupon extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    private Long discountPrice;

    private int discountRate;

    @Column(nullable = false)
    private boolean isUsed = false;

    private Coupon(Long userId, String name, CouponType couponType, Long discountPrice, int discountRate){
        this.userId = userId;
        this.name = name;
        this.couponType = couponType;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
    }

    public static Coupon from(Long userId, String name, String couponType, Long discountPrice, int discountRate) {
        validateName(name);
        validateDiscountPrice(discountPrice);
        validateDiscountRate(discountRate);
        return new Coupon(
                userId,
                name,
                CouponType.from(couponType),
                discountPrice,
                discountRate
        );
    }

    public static void validateName(String name){
        if(name == null){
            throw new IllegalArgumentException("쿠폰이름은 null일 수 없습니다.");
        } else if(name.isEmpty()){
            throw new IllegalArgumentException("쿠폰이름은 빈 문자열일 수 없습니다.");
        }
    }

    public static void validateDiscountPrice(Long discountPrice) {
        if(discountPrice < 0) {
            throw new IllegalArgumentException("쿠폰금액은 0보다 작을 수 없습니다.");
        }
    }

    public static void validateDiscountRate(int discountRate) {
        if(discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("쿠폰할인율은 0이상 100이하여야 합니다.");
        }
    }

    // 정률 할인 쿠폰
    public Long applyRateDiscount(Long originalPrice, int discountRate) {
        validateDiscountRate(discountRate);
        long discountedPrice = originalPrice * (100 - discountRate) / 100;
        return Math.max(0, discountedPrice);
    }
}
