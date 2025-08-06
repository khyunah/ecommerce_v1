package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderFacadeConcurrencyTest {
    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PointRepository pointRepository;

    private final int threadCount = 10;

    private Long userId1;
    private Long userId2;
    private Long productId1;
    private Long productId2;
    private Long pointId1;
    private Long pointId2;
    private Long stockId1;
    private Long stockId2;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {

        // 유저 저장
        User user = User.from("testUser01", "test1@example.com", "1990-01-01", "M");
        user = userRepository.save(user).orElseThrow();
        userId1 = user.getId();
        User user2 = User.from("testUser02", "test2@example.com", "2000-11-24", "F");
        user2 = userRepository.save(user2).orElseThrow();
        userId2 = user2.getId();

        // 포인트 저장 (20,000점)
        Point point = Point.from(userId1, 20000L);
        point = pointRepository.save(point);
        pointId1 = point.getId();
        Point point2 = Point.from(userId2, 20000L);
        point2 = pointRepository.save(point2);
        pointId2 = point2.getId();

        // 상품 저장
        Product product1 = Product.from(
                "청바지",
                "청바지 상품 설명",
                BigDecimal.valueOf(1000),    // 할인가
                BigDecimal.valueOf(1500),    // 원가
                "ON_SALE",
                1L                            // 브랜드 아이디
        );
        product1 = productRepository.save(product1);
        productId1 = product1.getId();

        Product product2 = Product.from(
                "티셔츠",
                "티셔츠 상품 설명",
                BigDecimal.valueOf(2000),    // 할인가
                BigDecimal.valueOf(2500),    // 원가
                "ON_SALE",
                1L                            // 브랜드 아이디
        );
        product2 = productRepository.save(product2);
        productId2 = product2.getId();

        // 재고 저장 (10개)
        Stock stock = Stock.from(productId1, 10);
        stock = stockRepository.save(stock);
        stockId1 = stock.getId();
        Stock stock2 = Stock.from(productId2, 10);
        stock2 = stockRepository.save(stock2);
        stockId2 = stock2.getId();

    }

    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감된다.")
    @Test
    void should_deduct_points_correctly_when_user_places_multiple_orders_concurrently() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    List<OrderItemResult> items = List.of(new OrderItemResult(productId1, 1));
                    orderFacade.placeOrder(userId1, items, "ORDER-SEQ-" + Thread.currentThread().getId());
                } catch (Exception e) {
                    // 예외 무시 (충돌이 날 수도 있음)
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 검증
        Stock stock = stockRepository.findById(stockId1).orElseThrow();
        Point point = pointRepository.findById(pointId1).orElseThrow();
        List<Order> orders = orderRepository.findAllByUserId(userId1);

        System.out.println("최종 재고 수량 = " + stock.getQuantity());
        System.out.println("최종 포인트 잔액 = " + point.getBalance().getValue());
        System.out.println("성공한 주문 수 = " + orders.size());

        assertThat(stock.getQuantity()).isEqualTo(0);
        assertThat(point.getBalance().getValue()).isEqualTo(10000);
        assertThat(orders.size()).isLessThanOrEqualTo(10); // 일부는 실패 가능
    }

}

