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
        // 실제 외부 시스템 연동은 여기서 구현합니다.
        // 현재는 Stub 처리 (로그만 출력)
        log.info("Stub - 외부 시스템으로 주문 전송: orderId = {}", order.getId());
    }
}
