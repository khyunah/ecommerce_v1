package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.vo.Balance;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.vo.Money;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.UserId;
import com.loopers.support.error.CoreException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class OrderFacadeTest {
    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean // 외부 시스템은 모킹
    private ExternalOrderSender externalOrderSender;
    @Autowired
    private UserService userService;

    @DisplayName("주문 성공")
    @Test
    void placeOrder_successfully_places_order() {
        // given
        User user = userService.register(User.from("userId123", "test@naver.com", "1995-01-01", "F"));
        Product product = productRepository.save(Product.from("티셔츠1","설명1",BigDecimal.valueOf(100L), BigDecimal.valueOf(150L),"ON_SALE", 20L));
        pointRepository.save(Point.from(user.getId(),50000L));
        stockRepository.save(Stock.from(product.getId(), 10));

        List<OrderItemResult> items = List.of(new OrderItemResult(product.getId(), 2));
        Long usePoint = 3000L;

        // when
        Order order = orderFacade.placeOrder(user.getId(), items, usePoint, "ORDER-123");

        // then
        assertThat(order.getId()).isNotNull();
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(pointRepository.findByRefUserId(user.getId()).get().getBalance().getValue()).isEqualTo(Balance.from(47000L).getValue()); // 포인트 차감 확인

        assertThat(stockRepository.findByRefProductId(product.getId()).get().getQuantity())
                .isEqualTo(8); // 재고 차감 확인

        verify(externalOrderSender).sendOrder(any(Order.class)); // 외부 시스템 호출 확인
    }
}
