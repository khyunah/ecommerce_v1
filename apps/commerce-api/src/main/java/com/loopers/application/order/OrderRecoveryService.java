package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderRecoveryService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final PointRepository pointRepository;
    private final CouponRepository couponRepository;

    /**
     * 결제 실패/취소 시 재고, 포인트, 쿠폰 복구 처리
     */
    @Transactional
    public void handlePaymentFailure(Payment payment, Order order) {
        System.out.println("결제 실패로 인한 복구 처리 시작 - orderId: " + order.getId());
        
        try {
            // 1. 재고 복구
            recoverStock(order);
            
            // 2. 포인트 복구
            recoverPoint(order);
            
            // 3. 쿠폰 복구
            recoverCoupon(order);
            
            System.out.println("복구 처리 완료 - orderId: " + order.getId());
            
        } catch (Exception e) {
            System.out.println("복구 처리 중 오류 - orderId: " + order.getId() + ", error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 재고 복구
     */
    private void recoverStock(Order order) {
        System.out.println("재고 복구 시작 - orderId: " + order.getId());
        
        for (OrderItem orderItem : order.getOrderItems()) {
            try {
                Optional<Stock> stockOpt = stockRepository.findByRefProductIdWithLock(orderItem.getProductId());
                
                if (stockOpt.isPresent()) {
                    Stock stock = stockOpt.get();
                    
                    // 재고 원복 (차감했던 수량만큼 다시 추가)
                    int currentQuantity = stock.getQuantity();
                    int recoveryQuantity = orderItem.getQuantity();
                    
                    stock.updateQuantity(currentQuantity , recoveryQuantity);
                    stockRepository.save(stock);
                    
                    System.out.println("재고 복구 완료 - productId: " + orderItem.getProductId() +
                                     ", 복구수량: " + recoveryQuantity + 
                                     ", 현재재고: " + stock.getQuantity());
                } else {
                    System.out.println("재고 정보를 찾을 수 없음 - productId: " + orderItem.getProductId());
                }
                
            } catch (Exception e) {
                System.out.println("재고 복구 실패 - productId: " + orderItem.getProductId() +
                                 ", error: " + e.getMessage());
            }
        }
    }

    /**
     * 포인트 복구
     */
    private void recoverPoint(Order order) {
        if (order.getUsedPointAmount() == null || order.getUsedPointAmount() <= 0) {
            System.out.println("복구할 포인트가 없음 - orderId: " + order.getId());
            return;
        }
        
        System.out.println("포인트 복구 시작 - orderId: " + order.getId() + 
                         ", 복구포인트: " + order.getUsedPointAmount());
        
        try {
            Optional<Point> pointOpt = pointRepository.findByRefUserIdWithLock(order.getRefUserId());
            
            if (pointOpt.isPresent()) {
                Point point = pointOpt.get();
                
                // 포인트 원복 (사용했던 포인트만큼 다시 추가)
                long currentBalance = point.getBalance().getValue();
                long recoveryPoint = order.getUsedPointAmount();
                
                point.getBalance().plus(recoveryPoint);
                pointRepository.save(point);
                
                System.out.println("포인트 복구 완료 - userId: " + order.getRefUserId() + 
                                 ", 복구포인트: " + recoveryPoint + 
                                 ", 현재잔액: " + point.getBalance().getValue());
            } else {
                System.out.println("포인트 정보를 찾을 수 없음 - userId: " + order.getRefUserId());
            }
            
        } catch (Exception e) {
            System.out.println("포인트 복구 실패 - userId: " + order.getRefUserId() + 
                             ", error: " + e.getMessage());
        }
    }

    /**
     * 쿠폰 복구
     */
    private void recoverCoupon(Order order) {
        if (order.getUsedCouponId() == null) {
            System.out.println("복구할 쿠폰이 없음 - orderId: " + order.getId());
            return;
        }
        
        System.out.println("쿠폰 복구 시작 - orderId: " + order.getId() + 
                         ", couponId: " + order.getUsedCouponId());
        
        try {
            Optional<Coupon> couponOpt = couponRepository.findById(order.getUsedCouponId());
            
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                
                // 쿠폰 상태를 다시 사용 가능하게 변경
                coupon.restoreCoupon();
                couponRepository.save(coupon);
                
                System.out.println("쿠폰 복구 완료 - couponId: " + order.getUsedCouponId());
            } else {
                System.out.println("쿠폰 정보를 찾을 수 없음 - couponId: " + order.getUsedCouponId());
            }
            
        } catch (Exception e) {
            System.out.println("쿠폰 복구 실패 - couponId: " + order.getUsedCouponId() + 
                             ", error: " + e.getMessage());
        }
    }
}
