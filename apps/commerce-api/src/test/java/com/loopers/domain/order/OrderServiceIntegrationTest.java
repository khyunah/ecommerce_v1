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
        List<OrderItem> items = List.of(new OrderItem(200L,1,"티셔츠", Money.from(BigDecimal.valueOf(1000)),Money.from(BigDecimal.valueOf(1200))));
        Order order = orderRepository.save(Order.create(20L,"seq1123", items, 1000L));

        // when
        List<Order> results = orderSpyService.getOrders(order.getRefUserId());

        // then
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getOrderSeq()).isEqualTo(order.getOrderSeq());
        assertThat(results.get(0).getOrderStatus()).isEqualTo(order.getOrderStatus());
    }

    @DisplayName("유저의 주문 상세정보를 반환한다.")
    @Test
    void returnsOrderDetail_WhenUserOwnsOrder() {
        // given
        List<OrderItem> items = List.of(new OrderItem(1L,1,"티셔츠", Money.from(BigDecimal.valueOf(1000)),Money.from(BigDecimal.valueOf(1200))));
        Order order = orderRepository.save(Order.create(1L,"seq1123", items, 1000L));

        // when
        Order result = orderSpyService.getOrderDetail(order.getId(), order.getRefUserId());

        // then
        assertThat(result.getOrderItems().size()).isEqualTo(1);
        assertThat(result.getOrderSeq()).isEqualTo(order.getOrderSeq());
        assertThat(result.getOrderStatus()).isEqualTo(order.getOrderStatus());
        assertThat(result.getOrderItems().get(0).getProductName()).isEqualTo(order.getOrderItems().get(0).getProductName());
    }
}
