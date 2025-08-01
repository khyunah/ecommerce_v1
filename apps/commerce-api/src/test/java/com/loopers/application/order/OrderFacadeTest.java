package com.loopers.application.order;

import com.loopers.domain.order.*;
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
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                OrderItem.create(10L, 2, "티셔츠1", Money.from(BigDecimal.valueOf(3000L)), Money.from(BigDecimal.valueOf(3000L))),
                OrderItem.create(11L, 3, "티셔츠2", Money.from(BigDecimal.valueOf(3000L)), Money.from(BigDecimal.valueOf(3000L))),
                OrderItem.create(12L, 1, "티셔츠3", Money.from(BigDecimal.valueOf(3000L)), Money.from(BigDecimal.valueOf(3000L)))
        ));

        // when
        when(userRepository.findByUserId(userIdVo)).thenReturn(Optional.of(user));

        when(stockRepository.findByRefProductId(10L)).thenReturn(Optional.of(stock1));
        when(stockRepository.findByRefProductId(11L)).thenReturn(Optional.of(stock2));
        when(stockRepository.findByRefProductId(12L)).thenReturn(Optional.of(stock3));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(11L)).thenReturn(Optional.of(product2));
        when(productRepository.findById(12L)).thenReturn(Optional.of(product3));

        when(pointRepository.findByRefUserId(userIdVo)).thenReturn(Optional.of(point));
        when(orderRepository.save(Mockito.any(Order.class))).thenReturn(dummyOrder);

        Order order = orderFacade.placeOrder(userIdVo.getValue(), items, pointsToUse);

        // then
        Assertions.assertNotNull(order);
        Mockito.verify(stockRepository, Mockito.times(3)).save(Mockito.any(Stock.class));
        Mockito.verify(pointRepository).save(Mockito.any(Point.class));
        Mockito.verify(orderRepository).save(Mockito.any(Order.class));
        Mockito.verify(externalOrderSender).sendOrder(Mockito.any(Order.class));
    }

    @Test
    @DisplayName("주문 목록 조회 성공 - 주문 2건, 각 주문에 상품 2개 포함")
    void getOrders_success() {
        // given
//        UserId userId = UserId.from("user123");
        Long userId = 1L;

        Order order1 = createOrder(1L, OrderStatus.ORDERED, LocalDateTime.of(2025, 8, 1, 10, 0),
                List.of(
                        createItem(1L, 5000),
                        createItem(2L, 7000)
                ));

        Order order2 = createOrder(2L, OrderStatus.DELIVERED, LocalDateTime.of(2025, 8, 2, 15, 30),
                List.of(
                        createItem(3L, 3000),
                        createItem(4L, 4000)
                ));

        when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(order1, order2));

        // when
        List<OrderSummaryResult> result = orderFacade.getOrders(userId);

        // then
        assertThat(result).hasSize(4);
        assertThat(result).extracting(OrderSummaryResult::productId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
        assertThat(result).extracting(OrderSummaryResult::status)
                .contains("ORDERED", "DELIVERED");
    }

    private Order createOrder(Long orderId, OrderStatus status, LocalDateTime orderedAt, List<OrderItem> items) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getOrderStatus()).thenReturn(status);
        when(order.getCreatedAt()).thenReturn(orderedAt.atZone(ZoneId.of("Asia/Seoul")));
        when(order.getOrderItems()).thenReturn(items);
        return order;
    }

    private OrderItem createItem(Long productId, int price) {
        OrderItem item = mock(OrderItem.class);
        when(item.getProductId()).thenReturn(productId);
        when(item.getSellingPrice()).thenReturn(Money.from(BigDecimal.valueOf(price)));
        return item;
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_success() {
        // given
        Long userId = 1L;
        Long orderId = 100L;

        OrderItem item1 = mock(OrderItem.class);
        when(item1.getProductName()).thenReturn("상품 A");
        when(item1.getQuantity()).thenReturn(2);
        when(item1.getOriginalPrice()).thenReturn(Money.from(BigDecimal.valueOf(10000)));
        when(item1.getSellingPrice()).thenReturn(Money.from(BigDecimal.valueOf(8000)));

        OrderItem item2 = mock(OrderItem.class);
        when(item2.getProductName()).thenReturn("상품 B");
        when(item2.getQuantity()).thenReturn(1);
        when(item2.getOriginalPrice()).thenReturn(Money.from(BigDecimal.valueOf(20000)));
        when(item2.getSellingPrice()).thenReturn(Money.from(BigDecimal.valueOf(15000)));

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);
        when(order.getOrderItems()).thenReturn(List.of(item1, item2));

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        // when
        OrderDetailResult result = orderFacade.getOrderDetail(userId, orderId);

        // then
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.orderStatus()).isEqualTo("DELIVERED");
        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).extracting("productName")
                .containsExactlyInAnyOrder("상품 A", "상품 B");
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 존재하지 않는 주문")
    void getOrderDetail_notFound() {
        // given
        Long userId = 1L;
        Long orderId = 999L;

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderFacade.getOrderDetail(userId, orderId))
                .isInstanceOf(CoreException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

}
