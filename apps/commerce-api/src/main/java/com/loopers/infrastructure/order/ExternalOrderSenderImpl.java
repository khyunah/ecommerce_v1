package com.loopers.infrastructure.order;

import com.loopers.domain.order.ExternalOrderSender;
import com.loopers.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ExternalOrderSenderImpl implements ExternalOrderSender {
    private static final Logger log = LoggerFactory.getLogger(ExternalOrderSenderImpl.class);

    @Override
    public void sendOrder(Order order) {
        log.info("Stub - 외부 시스템으로 주문 전송: orderId = {}, userId = {}, finalAmount = {}", 
                order.getId(), order.getRefUserId(), order.getFinalAmount());
    }

    @Override
    public void sendOrderCancellation(Order order, String reason, String message) {
        log.info("Stub - 외부 시스템으로 주문 취소 전송: orderId = {}, reason = {}, message = {}", 
                order.getId(), reason, message);
    }
}
