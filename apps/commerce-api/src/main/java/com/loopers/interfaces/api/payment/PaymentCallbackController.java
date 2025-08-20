package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCallbackService;
import com.loopers.domain.payment.dto.PgCallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentCallbackController {

    private final PaymentCallbackService paymentCallbackService;

    /**
     * PG사에서 결제 완료/실패 콜백을 받는 API
     */
    @PostMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(@RequestBody PgCallbackRequest request) {
        try {
            System.out.println("PG 콜백 수신 - orderId: " + request.orderId() + 
                             ", status: " + request.status());
            
            // 콜백 처리
            paymentCallbackService.processCallback(request);
            
            System.out.println("PG 콜백 처리 완료 - orderId: " + request.orderId());
            
            // PG사에게 성공 응답
            return ResponseEntity.ok("SUCCESS");
            
        } catch (Exception e) {
            System.out.println("PG 콜백 처리 실패 - orderId: " + request.orderId() + 
                             ", error: " + e.getMessage());
            
            // PG사에게 실패 응답 (PG사에서 재전송할 수 있도록)
            return ResponseEntity.badRequest().body("FAIL");
        }
    }

    /**
     * 수동으로 결제 상태를 확인하는 API
     */
    @PostMapping("/{paymentSeq}/status-check")
    public ResponseEntity<?> manualStatusCheck(@PathVariable String paymentSeq) {
        try {
            System.out.println("수동 결제 상태 확인 요청 - paymentSeq: " + paymentSeq);
            
            String result = paymentCallbackService.manualStatusCheck(paymentSeq);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.out.println("수동 결제 상태 확인 실패 - paymentSeq: " + paymentSeq + 
                             ", error: " + e.getMessage());
            
            return ResponseEntity.badRequest().body("상태 확인 실패: " + e.getMessage());
        }
    }
}
