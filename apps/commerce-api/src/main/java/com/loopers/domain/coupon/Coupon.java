package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "coupon")
public class Coupon extends BaseEntity {

    @Column(nullable = false)
    private Long refUserId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    private Long discountPrice;

    private int discountRate;

    @Column(nullable = false)
    private boolean isUsed = false;

    @Version
    private Long version;

    private Coupon(Long refUserId, String name, CouponType couponType, Long discountPrice, int discountRate){
        this.refUserId = refUserId;
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

    // 정률 할인 쿠폰 계산
    public Long applyRateDiscount(Long originalPrice, int discountRate) {
        if(!couponType.name().equals(CouponType.RATE.name())){
            throw new IllegalArgumentException("정률 할인 쿠폰이 아닙니다.");
        }
        validateDiscountRate(discountRate);
        long discountedPrice = originalPrice * (100 - discountRate) / 100;
        return Math.max(0, discountedPrice);
    }

    // 정액 할인 쿠폰 계산
    public Long applyPriceDiscount(Long originalPrice, Long discountPrice) {
        if(!couponType.name().equals(CouponType.PRICE.name())){
            throw new IllegalArgumentException("정액 할인 쿠폰이 아닙니다.");
        }
        validateDiscountPrice(discountPrice);
        long discountedPrice = originalPrice - discountPrice;
        return Math.max(0, discountedPrice);
    }

    public void useCoupon(){
        if(isUsed){
           throw new IllegalArgumentException("이미 사용한 쿠폰입니다.");
        }
        isUsed = true;
    }
}
