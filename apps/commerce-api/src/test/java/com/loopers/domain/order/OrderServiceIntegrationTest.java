package com.loopers.domain.order;

import com.loopers.domain.product.vo.Money;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    private OrderService orderSpyService;
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        OrderService realService = new OrderService(orderRepository);
        orderSpyService = Mockito.spy(realService);
    }

    @DisplayName("유저의 주문 목록을 반환한다.")
    @Test
    void returnsOrderList_WhenUserExists() {
        // given
        List<OrderItem> items = List.of(new OrderItem(1L,1,"티셔츠", Money.from(BigDecimal.valueOf(1000)),Money.from(BigDecimal.valueOf(1200))));
        Order order = orderRepository.save(Order.create(1L,"seq1123", items));

        // when
        List<Order> results = orderSpyService.getOrders(order.getRefUserId());

        // then
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getOrderSeq()).isEqualTo(order.getOrderSeq());
        assertThat(results.get(0).getOrderStatus()).isEqualTo(order.getOrderStatus());
    }
}
