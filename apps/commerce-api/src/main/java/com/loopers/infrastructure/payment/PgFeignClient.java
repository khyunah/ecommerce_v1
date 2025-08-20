package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.dto.PgPaymentRequest;
import com.loopers.domain.payment.dto.PgPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "pg-client", 
    url = "${pg.payment.url:http://localhost:8082}",
    configuration = PgFeignConfig.class
)
public interface PgFeignClient {
    
    @PostMapping("/api/payments")
    PgPaymentResponse requestPayment(@RequestBody PgPaymentRequest request);
    
    @GetMapping("/api/payments/{orderId}")
    PgPaymentResponse getPaymentStatus(@PathVariable("orderId") String orderId);
}
