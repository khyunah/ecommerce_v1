package com.loopers.application.order;

import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.application.order.out.OrderCreateResult;
import com.loopers.domain.order.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.vo.Balance;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;

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
        pointRepository.save(Point.from(user.getId(),5000L));
        stockRepository.save(Stock.from(product.getId(), 10));

        List<OrderItemCriteria> items = List.of(new OrderItemCriteria(product.getId(), 2));

        // when
        OrderCreateCommand command = new OrderCreateCommand(
                user.getId(),
                items,
                "ORDER-123",
                -1L,
                100L
        );

        OrderCreateResult result = orderFacade.placeOrder(command);

        // then
        assertThat(result.orderId()).isNotNull();
        assertThat(pointRepository.findByRefUserId(user.getId()).get().getBalance().getValue()).isEqualTo(Balance.from(4800L).getValue()); // 포인트 차감 확인

        assertThat(stockRepository.findByRefProductIdWithLock(product.getId()).get().getQuantity())
                .isEqualTo(8); // 재고 차감 확인

        verify(externalOrderSender).sendOrder(any(Order.class)); // 외부 시스템 호출 확인
    }
}
