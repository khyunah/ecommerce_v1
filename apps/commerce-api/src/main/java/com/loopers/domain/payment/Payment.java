package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "payment")
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private Long refOrderId;

    @Column(nullable = false, unique = true)
    private String paymentSeq;              // 결제 고유번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;    // 결제 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;    // 결제 수단

    @Column(nullable = false)
    private Long paymentAmount;             // 실제 PG사로 결제된 금액

    @Column
    private String pgProvider;              // PG사

    @Column
    private String pgTid;                   // PG사 거래 ID

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime failedAt;

    @Column
    private LocalDateTime canceledAt;

    // 실패/취소 사유
    @Column
    private String failureReason;

    @Column
    private String cancelReason;

    public static Payment create(Long refOrderId, String paymentSeq,
                                 String paymentMethod, Long paymentAmount,
                                 String pgProvider) {
        Payment payment = new Payment();
        payment.refOrderId = refOrderId;
        payment.paymentSeq = paymentSeq;
        payment.paymentStatus = PaymentStatus.PENDING;
        payment.paymentMethod = PaymentMethod.from(paymentMethod);
        payment.paymentAmount = paymentAmount;
        payment.pgProvider = pgProvider;
        return payment;
    }

    // 결제 성공 처리
    public void completePayment(String pgTid) {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.pgTid = pgTid;
        this.paidAt = LocalDateTime.now();
    }

    // 결제 실패 처리
    public void failPayment(String failureReason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.failedAt = LocalDateTime.now();
    }

    // 결제 취소 처리
    public void cancelPayment(String cancelReason) {
        this.paymentStatus = PaymentStatus.CANCELED;
        this.cancelReason = cancelReason;
        this.canceledAt = LocalDateTime.now();
    }

}
