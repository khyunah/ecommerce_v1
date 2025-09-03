package com.loopers.application.order.event;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.event.CouponUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsageEventHandler {
    
    private final CouponService couponService;

    @EventListener
    public void handleCouponUsageEvent(CouponUsageEvent event) {
        try {
            log.info("쿠폰 사용 이벤트 처리 시작 - userId: {}, couponId: {}", 
                    event.getUserId(), event.getCouponId());

            // 쿠폰 조회 및 사용 처리 (메인 트랜잭션 내에서)
            Coupon coupon = couponService.get(event.getCouponId(), event.getUserId());
            coupon.useCoupon();
            couponService.save(coupon);

            // 할인 금액 계산
            Long discountAmount = coupon.applyCoupon(coupon, event.getTotalOrderAmount());
            event.updateDiscountAmount(discountAmount);

            log.info("쿠폰 사용 완료 - couponId: {}, 할인금액: {}", 
                    event.getCouponId(), discountAmount);

            
        } catch (Exception e) {
            log.error("쿠폰 사용 이벤트 처리 실패 - userId: {}, couponId: {}", 
                    event.getUserId(), event.getCouponId(), e);
            throw e; // 예외 전파하여 메인 트랜잭션 롤백
        }
    }
}
