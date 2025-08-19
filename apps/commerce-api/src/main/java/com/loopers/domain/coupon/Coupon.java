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

    private Long discountValue;

    private int discountRate;

    @Column(nullable = false)
    private boolean isUsed = false;

    @Version
    private Long version;

    private Coupon(Long refUserId, String name, CouponType couponType, Long discountValue){
        this.refUserId = refUserId;
        this.name = name;
        this.couponType = couponType;
        this.discountValue = discountValue;
    }

    public static Coupon from(Long userId, String name, String couponType, Long discountValue) {
        validateName(name);
        CouponType couponType1 = CouponType.from(couponType);
        if(couponType1 == CouponType.PRICE){
            validateDiscountPrice(discountValue);
        } else if(couponType1 == CouponType.RATE){
            validateDiscountRate(discountValue);
        }
        return new Coupon(
                userId,
                name,
                couponType1,
                discountValue
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

    public static void validateDiscountRate(Long discountRate) {
        if(discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("쿠폰할인율은 0이상 100이하여야 합니다.");
        }
    }

    public Long applyCoupon(Coupon coupon, Long originalPrice) {
        Long discountAmount = 0L;
        if(coupon == null){
            throw new IllegalArgumentException("쿠폰이 존재하지 않습니다.");
        } else if(this.couponType == CouponType.PRICE){
            discountAmount = applyPriceDiscount(originalPrice, coupon.discountValue);
        } else if(this.couponType == CouponType.RATE){
            discountAmount = applyRateDiscount(originalPrice, coupon.discountValue);
        }
        return discountAmount;
    }

    // 정률 할인 쿠폰 계산
    public Long applyRateDiscount(Long originalPrice, Long discountRate) {
        if(!couponType.name().equals(CouponType.RATE.name())){
            throw new IllegalArgumentException("정률 할인 쿠폰이 아닙니다.");
        }
        validateDiscountRate(discountRate);
        long discountAmount = originalPrice * discountRate / 100;
        return Math.min(discountAmount, originalPrice); // 원가보다 클 수 없음
    }

    // 정액 할인 쿠폰 계산
    public Long applyPriceDiscount(Long originalPrice, Long discountPrice) {
        if(!couponType.name().equals(CouponType.PRICE.name())){
            throw new IllegalArgumentException("정액 할인 쿠폰이 아닙니다.");
        }
        validateDiscountPrice(discountPrice);
        return Math.min(discountPrice, originalPrice); // 원가보다 클 수 없음
    }

    public void useCoupon(){
        if(isUsed){
           throw new IllegalArgumentException("이미 사용한 쿠폰입니다.");
        }
        isUsed = true;
    }
}
