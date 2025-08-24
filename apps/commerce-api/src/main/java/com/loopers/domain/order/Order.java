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

    @Column(nullable = false, unique = true)
    private String orderSeq;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Long totalAmount;           // 총 주문 금액

    @Column
    private Long discountAmount;        // 총 할인 금액

    @Column(nullable = false)
    private Long finalAmount;           // 최종 결제 금액

    @Column
    private Long usedCouponId;          // 사용한 쿠폰 ID

    @Column
    private Long couponDiscountAmount;  // 쿠폰 할인 금액

    @Column
    private Long usedPointAmount;       // 사용한 포인트 금액

    public static Order create(Long refUserId, String orderSeq, List<OrderItem> orderItems, Long totalAmount) {
        Order order = new Order();
        order.refUserId = refUserId;
        order.orderSeq = orderSeq;
        order.orderStatus = OrderStatus.PENDING;  // 결제 대기 상태로 시작
        order.orderItems = orderItems;
        order.totalAmount = totalAmount;
        order.finalAmount = totalAmount;
        return order;
    }

    // 할인 쿠폰 적용
    public void applyCoupon(Long couponId, Long discountAmount) {
        this.usedCouponId = couponId;
        this.couponDiscountAmount = discountAmount;
        recalculateAmount();
    }

    // 포인트 적용
    public void applyPoint(Long pointAmount) {
        this.usedPointAmount = pointAmount;
        recalculateAmount();
    }

    // 금액 재계산
    private void recalculateAmount() {
        Long totalDiscount = 0L;
        if (couponDiscountAmount != null) {
            totalDiscount += couponDiscountAmount;
        }
        if (usedPointAmount != null) {
            totalDiscount += usedPointAmount;
        }

        this.discountAmount = totalDiscount;
        this.finalAmount = totalAmount - totalDiscount;

        // 최종 금액이 0보다 작을 수 없도록 처리
        if (this.finalAmount < 0) {
            this.finalAmount = 0L;
        }
    }

    // 주문 상태 변경
    public void updateStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }

    // 결제 완료 처리
    public void completePayment() {
        this.orderStatus = OrderStatus.PAID;
    }

    // 주문 취소
    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
    }

}
