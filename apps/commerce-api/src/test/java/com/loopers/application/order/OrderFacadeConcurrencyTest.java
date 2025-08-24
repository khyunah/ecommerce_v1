package com.loopers.application.order;

import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponType;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class OrderFacadeConcurrencyTest {
    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CouponRepository couponRepository;

    private final int threadCount = 10;

    private Long userId1;
    private Long userId2;
    private Long productId1;
    private Long productId2;
    private Long pointId1;
    private Long pointId2;
    private Long stockId1;
    private Long stockId2;
    private Long couponId1;
    private Long couponId2;

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
                BigDecimal.valueOf(10000),    // 할인가
                BigDecimal.valueOf(15000),    // 원가
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

        // 쿠폰 저장
        Coupon coupon = Coupon.from(userId1,"정액할인쿠폰", CouponType.PRICE.name(), 5000L);
        coupon = couponRepository.save(coupon);
        couponId1 = coupon.getId();
    }

    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감된다.")
    @Test
    void should_deduct_points_correctly_when_user_places_multiple_orders_concurrently() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    List<OrderItemCriteria> items = List.of(new OrderItemCriteria(productId1, 1));
                    OrderCreateCommand command = new OrderCreateCommand(
                            userId1,
                            items,
                            "ORDER-SEQ-" + Thread.currentThread().getId(),
                            -1L,
                            1000L,
                            "POINT_ONLY",
                            "KHY_PG",
                            null,
                            null
                    );
                    orderFacade.placeOrder(command);
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

    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행할때, 포인트가 부족하면 주문이 완료되지 않는다.")
    @Test
    void should_fail_order_if_points_are_insufficient_in_concurrent_orders_by_same_user() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    List<OrderItemCriteria> items = List.of(new OrderItemCriteria(productId2, 1));
                    OrderCreateCommand command = new OrderCreateCommand(
                            userId1,
                            items,
                            "ORDER-SEQ-" + Thread.currentThread().getId(),
                            -1L,
                            10000L,
                            "POINT_ONLY",
                            "KHY_PG",
                            null,
                            null
                    );
                    orderFacade.placeOrder(command);
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
        Stock stock = stockRepository.findById(stockId2).orElseThrow();
        Point point = pointRepository.findById(pointId1).orElseThrow();
        List<Order> orders = orderRepository.findAllByUserId(userId1);

        System.out.println("최종 재고 수량 = " + stock.getQuantity());
        System.out.println("최종 포인트 잔액 = " + point.getBalance().getValue());
        System.out.println("성공한 주문 수 = " + orders.size());

        assertThat(stock.getQuantity()).isEqualTo(8);
        assertThat(point.getBalance().getValue()).isEqualTo(0);
        assertThat(orders.size()).isLessThanOrEqualTo(2); // 일부는 실패 가능
    }

    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감된다.")
    @Test
    void should_deduct_stock_correctly_when_multiple_orders_for_same_product_are_placed_concurrently() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 두명의 유저가 하나의 상품을 여러번 주문
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    Long userId = 0L;
                    List<OrderItemCriteria> items = null;
                    if(finalI%2 == 0){
                        System.out.println("짝수: "+finalI%2);
                        userId = userId2;
                        items = List.of(new OrderItemCriteria(productId1, 1));
                    } else {
                        System.out.println("홀수: "+finalI%2);
                        userId = userId1;
                        items = List.of(new OrderItemCriteria(productId1, 1));
                    }
                    OrderCreateCommand command = new OrderCreateCommand(
                            userId,
                            items,
                            "ORDER-SEQ-" + Thread.currentThread().getId(),
                            -1L,
                            1000L,
                            "POINT_ONLY",
                            "KHY_PG",
                            null,
                            null
                    );
                    orderFacade.placeOrder(command);
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
        Point point1 = pointRepository.findById(pointId1).orElseThrow();
        Point point2 = pointRepository.findById(pointId2).orElseThrow();
        List<Order> orders1 = orderRepository.findAllByUserId(userId1);
        List<Order> orders2 = orderRepository.findAllByUserId(userId2);

        System.out.println("최종 재고 수량 = " + stock.getQuantity());
        System.out.println("user1 최종 포인트 잔액 = " + point1.getBalance().getValue());
        System.out.println("user2 최종 포인트 잔액 = " + point2.getBalance().getValue());
        System.out.println("user1 성공한 주문 수 = " + orders1.size());
        System.out.println("user2 성공한 주문 수 = " + orders2.size());

        assertThat(stock.getQuantity()).isEqualTo(0);
        assertThat(point1.getBalance().getValue()).isEqualTo(15000);
        assertThat(point2.getBalance().getValue()).isEqualTo(15000);
        assertThat(orders1.size()).isLessThanOrEqualTo(5); // 일부는 실패 가능
        assertThat(orders2.size()).isLessThanOrEqualTo(5); // 일부는 실패 가능
    }

    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청될때, 재고가 없으면 주문완료되지 않는다.")
    @Test
    void should_fail_concurrent_orders_when_stock_is_insufficient() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    List<OrderItemCriteria> items = List.of(new OrderItemCriteria(productId1, 2));
                    OrderCreateCommand command = new OrderCreateCommand(
                            userId1,
                            items,
                            "ORDER-SEQ-" + Thread.currentThread().getId(),
                            -1L,
                            1000L,
                            "POINT_ONLY",
                            "KHY_PG",
                            null,
                            null
                    );
                    orderFacade.placeOrder(command);
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
        Point point1 = pointRepository.findById(pointId1).orElseThrow();
        List<Order> orders1 = orderRepository.findAllByUserId(userId1);

        System.out.println("최종 재고 수량 = " + stock.getQuantity());
        System.out.println("user1 최종 포인트 잔액 = " + point1.getBalance().getValue());
        System.out.println("user1 성공한 주문 수 = " + orders1.size());

        assertThat(stock.getQuantity()).isEqualTo(0);
        assertThat(point1.getBalance().getValue()).isEqualTo(15000);
        assertThat(orders1.size()).isLessThanOrEqualTo(5); // 일부는 실패 가능
    }

    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    @Test
    void should_use_coupon_only_once_when_multiple_orders_are_placed_simultaneously_with_same_coupon() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    List<OrderItemCriteria> items = List.of(new OrderItemCriteria(productId1, 1));
                    OrderCreateCommand command = new OrderCreateCommand(
                            userId1,
                            items,
                            "ORDER-SEQ-" + Thread.currentThread().getId(),
                            couponId1,
                            1000L,
                            "POINT_ONLY",
                            "KHY_PG",
                            null,
                            null
                    );
                    orderFacade.placeOrder(command);
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
        Coupon coupon = couponRepository.findById(couponId1).orElseThrow();
        List<Order> orders = orderRepository.findAllByUserId(userId1);

        System.out.println("최종 재고 수량 = " + stock.getQuantity());
        System.out.println("최종 포인트 잔액 = " + point.getBalance().getValue());
        System.out.println("쿠폰 사용 상태 = " + coupon.isUsed());
        System.out.println("성공한 주문 수 = " + orders.size());

        assertThat(stock.getQuantity()).isEqualTo(9);
        assertThat(point.getBalance().getValue()).isEqualTo(19000);
        assertThat(orders.size()).isLessThanOrEqualTo(1); // 일부는 실패 가능
    }
}

