package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaOrderRepository;

    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaOrderRepository.findById(id);
    }

    @Override
    public List<Order> findAllByUserId(Long refUserId) {
        return jpaOrderRepository.findByRefUserId(refUserId);
    }
}
