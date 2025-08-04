package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {
    private final OrderRepository orderRepository;

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> findAllByUserId(Long refUserId) {
        return orderRepository.findAllByUserId(refUserId);
    }

    public Order findByIdAndUserId(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId,userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문 정보가 존재하지 않습니다."));
    }
}
