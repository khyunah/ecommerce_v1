package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.user.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order,Long> {
    List<Order> findByRefUserId(Long refUserId);
}
