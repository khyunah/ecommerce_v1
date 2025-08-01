package com.loopers.application.order;

import com.loopers.domain.order.ExternalOrderSender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.vo.Money;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.vo.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {
    @InjectMocks
    private OrderFacade orderFacade;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private ExternalOrderSender externalOrderSender;

    @DisplayName("상품을 여러개 한꺼번에 주문할 수 있다.")
    @Test
    void placeOrder_shouldSucceed_whenValidInput() {
        // given
        Long userId = 1L;
        UserId userIdVo = UserId.from("test123");
        List<OrderItemResult> items = List.of(
                new OrderItemResult(10L, 2),
                new OrderItemResult(11L, 1),
                new OrderItemResult(12L, 1)
        );
        int pointsToUse = 1000;

        Product product1 = Product.from("상품명","설명1",BigDecimal.valueOf(5000L),BigDecimal.valueOf(9000L),"ON_SALE", 1L);
        Product product2 = Product.from("상품명","설명1",BigDecimal.valueOf(5000L),BigDecimal.valueOf(9000L),"ON_SALE", 1L);
        Product product3 = Product.from("상품명","설명1",BigDecimal.valueOf(5000L),BigDecimal.valueOf(9000L),"ON_SALE", 1L);

        Stock stock1 = Stock.from(10L, 10);
        Stock stock2 = Stock.from(11L, 10);
        Stock stock3 = Stock.from(12L, 10);

        User user = User.from("test123", "test@naver.com", "2000-12-28", "F");
        Point point = Point.from("test123", 30000L);
        Order dummyOrder = Order.create(userId, List.of(
                OrderItem.create(10L, 2, Money.from(BigDecimal.valueOf(3000L)), Money.from(BigDecimal.valueOf(3000L))),
                OrderItem.create(11L, 3, Money.from(BigDecimal.valueOf(3000L)), Money.from(BigDecimal.valueOf(3000L))),
                OrderItem.create(12L, 1, Money.from(BigDecimal.valueOf(3000L)), Money.from(BigDecimal.valueOf(3000L)))
        ));

        // when
        Mockito.when(userRepository.findByUserId(userIdVo)).thenReturn(Optional.of(user));

        Mockito.when(stockRepository.findByRefProductId(10L)).thenReturn(Optional.of(stock1));
        Mockito.when(stockRepository.findByRefProductId(11L)).thenReturn(Optional.of(stock2));
        Mockito.when(stockRepository.findByRefProductId(12L)).thenReturn(Optional.of(stock3));

        Mockito.when(productRepository.findById(10L)).thenReturn(Optional.of(product1));
        Mockito.when(productRepository.findById(11L)).thenReturn(Optional.of(product2));
        Mockito.when(productRepository.findById(12L)).thenReturn(Optional.of(product3));

        Mockito.when(pointRepository.findByRefUserId(userIdVo)).thenReturn(Optional.of(point));
        Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenReturn(dummyOrder);

        Order order = orderFacade.placeOrder(userIdVo.getValue(), items, pointsToUse);

        // then
        Assertions.assertNotNull(order);
        Mockito.verify(stockRepository, Mockito.times(3)).save(Mockito.any(Stock.class));
        Mockito.verify(pointRepository).save(Mockito.any(Point.class));
        Mockito.verify(orderRepository).save(Mockito.any(Order.class));
        Mockito.verify(externalOrderSender).sendOrder(Mockito.any(Order.class));
    }
}
