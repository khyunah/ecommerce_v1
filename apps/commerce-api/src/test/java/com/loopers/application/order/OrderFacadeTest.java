package com.loopers.application.order;

import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.application.order.out.OrderCreateResult;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PgPaymentResponse;
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
import com.loopers.support.error.CoreException;
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

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean // 외부 시스템은 모킹
    private ExternalOrderSender externalOrderSender;
    
    @MockBean // PG 클라이언트 모킹
    private PgClient pgClient;
    
    @Autowired
    private UserService userService;

    @DisplayName("주문 성공 - 포인트만 사용")
    @Test
    void placeOrder_successfully_places_order_with_point_only() {
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
                100L,
                "POINT_ONLY",  // 포인트만 사용하는 결제
                "KHY_PG",
                null,  // 포인트 결제는 카드 타입 불필요
                null   // 포인트 결제는 카드 번호 불필요
        );

        OrderCreateResult result = orderFacade.placeOrder(command);

        // then
        assertThat(result.orderId()).isNotNull();
        assertThat(pointRepository.findByRefUserId(user.getId()).get().getBalance().getValue()).isEqualTo(Balance.from(4900L).getValue()); // 포인트 차감 확인

        assertThat(stockRepository.findByRefProductIdWithLock(product.getId()).get().getQuantity())
                .isEqualTo(8); // 재고 차감 확인

        // 결제가 생성되었는지 확인
        List<Payment> payments = paymentRepository.findByOrderId(result.orderId());
        assertThat(payments).hasSize(1);
        
        // 주문이 완료 상태인지 확인
        Order order = orderRepository.findById(result.orderId()).get();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        verify(externalOrderSender).sendOrder(any(Order.class)); // 외부 시스템 호출 확인
        // PG 클라이언트는 호출되지 않아야 함
        verify(pgClient, never()).requestPayment(any());
    }

    @DisplayName("주문 성공 - 카드 결제 (금액 있음)")
    @Test
    void placeOrder_successfully_with_card_payment() {
        // given
        User user = userService.register(User.from("userId456", "test2@naver.com", "1995-01-01", "M"));
        Product product = productRepository.save(Product.from("티셔츠2","설명2",BigDecimal.valueOf(1000L), BigDecimal.valueOf(1500L),"ON_SALE", 20L));
        pointRepository.save(Point.from(user.getId(),500L));
        stockRepository.save(Stock.from(product.getId(), 10));

        List<OrderItemCriteria> items = List.of(new OrderItemCriteria(product.getId(), 1));

        // PG 응답 모킹
        when(pgClient.requestPayment(any())).thenReturn(
                new PgPaymentResponse(
                        new PgPaymentResponse.PgMeta("SUCCESS", null, null),
                        new PgPaymentResponse.PgData("20250820:TR:8eb171", "PENDING")
                )
        );

        // when
        OrderCreateCommand command = new OrderCreateCommand(
                user.getId(),
                items,
                "ORDER-456",
                -1L,
                500L,  // 포인트 500원 사용
                "CARD",  // 카드 결제
                "KHY_PG",
                "SAMSUNG",  // 카드 타입
                "1234-5678-9012-3456"  // 카드 번호
        );

        OrderCreateResult result = orderFacade.placeOrder(command);

        // then
        assertThat(result.orderId()).isNotNull();
        
        // 결제가 생성되었는지 확인
        List<Payment> payments = paymentRepository.findByOrderId(result.orderId());
        assertThat(payments).hasSize(1);
        Payment payment = payments.get(0);
        assertThat(payment.getPaymentAmount()).isEqualTo(500L); // 1000 - 500(포인트) = 500원
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING); // PG 요청 성공 후 PENDING 상태 유지

        // 주문이 결제 대기 상태인지 확인 (PG 요청 성공 후 콜백 대기)
        Order order = orderRepository.findById(result.orderId()).get();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

        verify(externalOrderSender).sendOrder(any(Order.class));
        verify(pgClient).requestPayment(any()); // PG 클라이언트 호출 확인
    }

    @DisplayName("주문 실패 - 카드 결제 PG 요청 실패 (Bad Request)")
    @Test
    void placeOrder_fails_when_pg_request_fails_with_bad_request() {
        // given
        User user = userService.register(User.from("userId555", "test5@naver.com", "1995-01-01", "M"));
        Product product = productRepository.save(Product.from("티셔츠5","설명5",BigDecimal.valueOf(1000L), BigDecimal.valueOf(1500L),"ON_SALE", 20L));
        pointRepository.save(Point.from(user.getId(),500L));
        stockRepository.save(Stock.from(product.getId(), 10));

        List<OrderItemCriteria> items = List.of(new OrderItemCriteria(product.getId(), 1));

        // PG 응답 모킹 - Bad Request 실패
        when(pgClient.requestPayment(any())).thenReturn(
                new PgPaymentResponse(
                        new PgPaymentResponse.PgMeta("FAIL", "Bad Request", "주문 ID는 6자리 이상 문자열이어야 합니다."),
                        null
                )
        );

        // when & then
        OrderCreateCommand command = new OrderCreateCommand(
                user.getId(),
                items,
                "ORDER-555",
                -1L,
                500L,
                "CARD",  // 카드 결제
                "KHY_PG",
                "SAMSUNG",  // 카드 타입
                "1234-5678-9012-3456"  // 카드 번호
        );

        assertThatThrownBy(() -> orderFacade.placeOrder(command))
                .isInstanceOf(CoreException.class);

        verify(pgClient).requestPayment(any());
    }

    @DisplayName("주문 실패 - 카드 결제 PG 요청 실패 (Internal Server Error)")
    @Test
    void placeOrder_fails_when_pg_request_fails_with_internal_server_error() {
        // given
        User user = userService.register(User.from("userId666", "test6@naver.com", "1995-01-01", "F"));
        Product product = productRepository.save(Product.from("티셔츠6","설명6",BigDecimal.valueOf(1000L), BigDecimal.valueOf(1500L),"ON_SALE", 20L));
        pointRepository.save(Point.from(user.getId(),500L));
        stockRepository.save(Stock.from(product.getId(), 10));

        List<OrderItemCriteria> items = List.of(new OrderItemCriteria(product.getId(), 1));

        // PG 응답 모킹 - Internal Server Error 실패
        when(pgClient.requestPayment(any())).thenReturn(
                new PgPaymentResponse(
                        new PgPaymentResponse.PgMeta("FAIL", "Internal Server Error", "현재 서버가 불안정합니다. 잠시 후 다시 시도해주세요."),
                        null
                )
        );

        // when & then
        OrderCreateCommand command = new OrderCreateCommand(
                user.getId(),
                items,
                "ORDER-666",
                -1L,
                500L,
                "CARD",  // 카드 결제
                "KHY_PG",
                "SAMSUNG",  // 카드 타입
                "1234-5678-9012-3456"  // 카드 번호
        );

        assertThatThrownBy(() -> orderFacade.placeOrder(command))
                .isInstanceOf(CoreException.class);

        verify(pgClient).requestPayment(any());
    }

    @DisplayName("주문 성공 - 카드 결제이지만 금액이 0원")
    @Test
    void placeOrder_successfully_with_card_payment_zero_amount() {
        // given
        User user = userService.register(User.from("userId789", "test3@naver.com", "1995-01-01", "F"));
        Product product = productRepository.save(Product.from("티셔츠3","설명3",BigDecimal.valueOf(1000L), BigDecimal.valueOf(1500L),"ON_SALE", 20L));
        pointRepository.save(Point.from(user.getId(),1000L));
        stockRepository.save(Stock.from(product.getId(), 10));

        List<OrderItemCriteria> items = List.of(new OrderItemCriteria(product.getId(), 1));

        // when
        OrderCreateCommand command = new OrderCreateCommand(
                user.getId(),
                items,
                "ORDER-789",
                -1L,
                1000L,  // 포인트로 전액 결제
                "CARD",  // 카드 결제이지만 금액이 0원
                "KHY_PG",
                "SAMSUNG",  // 카드 타입
                "1234-5678-9012-3456"  // 카드 번호
        );

        OrderCreateResult result = orderFacade.placeOrder(command);

        // then
        assertThat(result.orderId()).isNotNull();
        
        // 결제가 생성되었는지 확인
        List<Payment> payments = paymentRepository.findByOrderId(result.orderId());
        assertThat(payments).hasSize(1);
        Payment payment = payments.get(0);
        assertThat(payment.getPaymentAmount()).isEqualTo(0L); // 결제 금액 0원
        
        // 주문이 완료 상태인지 확인 (금액이 0원이므로 PG사 연결 없이 완료)
        Order order = orderRepository.findById(result.orderId()).get();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        verify(externalOrderSender).sendOrder(any(Order.class));
        // 금액이 0원이므로 PG 클라이언트는 호출되지 않아야 함
        verify(pgClient, never()).requestPayment(any());
    }

    @DisplayName("주문 성공 - 다른 결제 방법 (PG사 연결 불필요)")
    @Test
    void placeOrder_successfully_with_other_payment_method() {
        // given
        User user = userService.register(User.from("userId999", "test4@naver.com", "1995-01-01", "M"));
        Product product = productRepository.save(Product.from("티셔츠4","설명4",BigDecimal.valueOf(1000L), BigDecimal.valueOf(1500L),"ON_SALE", 20L));
        pointRepository.save(Point.from(user.getId(),500L));
        stockRepository.save(Stock.from(product.getId(), 10));

        List<OrderItemCriteria> items = List.of(new OrderItemCriteria(product.getId(), 1));

        // when
        OrderCreateCommand command = new OrderCreateCommand(
                user.getId(),
                items,
                "ORDER-999",
                -1L,
                500L,
                "POINT_ONLY",  // 카카오페이 (PG사 연결 불필요)
                "KHY_PG",
                null,  // 카카오페이는 카드 타입 불필요
                null   // 카카오페이는 카드 번호 불필요
        );

        OrderCreateResult result = orderFacade.placeOrder(command);

        // then
        assertThat(result.orderId()).isNotNull();
        
        // 결제가 생성되었는지 확인
        List<Payment> payments = paymentRepository.findByOrderId(result.orderId());
        assertThat(payments).hasSize(1);
        Payment payment = payments.get(0);
        assertThat(payment.getPaymentAmount()).isEqualTo(500L);
        
        // 카카오페이는 CARD가 아니므로 PG사 연결 없이 바로 완료
        Order order = orderRepository.findById(result.orderId()).get();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        verify(externalOrderSender).sendOrder(any(Order.class));
        // 카카오페이는 PG 클라이언트 호출되지 않아야 함
        verify(pgClient, never()).requestPayment(any());
    }
}
