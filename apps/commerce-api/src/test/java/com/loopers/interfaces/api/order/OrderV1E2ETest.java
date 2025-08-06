package com.loopers.interfaces.api.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.in.OrderCreateCommand;
import com.loopers.application.order.in.OrderItemCriteria;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderV1E2ETest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderFacade orderFacade;

    private Long userId1;
    private Long productId1;
    private Long pointId1;
    private Long stockId1;
    private Long couponId1;

    @BeforeEach
    void setUp() {

        // 유저 저장
        User user = User.from("testUser01", "test1@example.com", "1990-01-01", "M");
        user = userRepository.save(user).orElseThrow();
        userId1 = user.getId();

        // 포인트 저장 (20,000점)
        Point point = Point.from(userId1, 20000L);
        point = pointRepository.save(point);
        pointId1 = point.getId();

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

        // 재고 저장 (10개)
        Stock stock = Stock.from(productId1, 10);
        stock = stockRepository.save(stock);
        stockId1 = stock.getId();

        // 쿠폰 저장
        Coupon coupon = Coupon.from(userId1,"정액할인쿠폰", CouponType.PRICE.name(), 5000L, 0);
        coupon = couponRepository.save(coupon);
        couponId1 = coupon.getId();
    }

    @DisplayName("/api/v1/orders/주문요청")
    @Nested
    class create {

        @DisplayName("존재하는 유저가 주문을 요청할 경우, 주문이 생성된다.")
        @Test
        void order_is_created_when_user_exists_and_places_order() throws Exception {
            // given
            List<OrderV1Dto.OrderItemRequest> items = List.of(new OrderV1Dto.OrderItemRequest(productId1, 1));
            OrderV1Dto.OrderCreateRequest request = new OrderV1Dto.OrderCreateRequest(
                    items,
                    "ORDER-SEQ-123",
                    -1L
            );

            // when & then
            mockMvc.perform(post("/api/v1/orders")
                            .header("X-USER-ID", userId1.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returns400BadRequest_whenIsNullUserId() throws Exception {
            // given
            List<OrderV1Dto.OrderItemRequest> items = List.of(new OrderV1Dto.OrderItemRequest(productId1, 1));
            OrderV1Dto.OrderCreateRequest request = new OrderV1Dto.OrderCreateRequest(
                    items,
                    "ORDER-SEQ-123",
                    -1L
            );

            // when & then
            mockMvc.perform(post("/api/v1/orders")
                            .header("X-USER-ID", "")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @DisplayName("/api/v1/orders/목록조회")
    @Nested
    class get {

        @DisplayName("존재하는 유저가 주문목록을 조회할 경우, 목록이 반환된다.")
        @Test
        void returns_order_list_for_existing_user() throws Exception {
            // given
            List<OrderItemCriteria> items = List.of(new OrderItemCriteria(productId1, 1));
            OrderCreateCommand command = new OrderCreateCommand(
                    userId1,
                    items,
                    "ORDER-SEQ-1234",
                    -1L
            );
            orderFacade.placeOrder(command);

            // when & then
            mockMvc.perform(get("/api/v1/orders")
                            .header("X-USER-ID", userId1.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].orderId").exists())
                    .andExpect(jsonPath("$[0].status").exists())
                    .andExpect(jsonPath("$[0].orderedAt").exists())
                    .andExpect(jsonPath("$[0].price").exists())
                    .andDo(print());
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returns400BadRequest_whenIsNullUserId() throws Exception {
            // given
            List<OrderV1Dto.OrderItemRequest> items = List.of(new OrderV1Dto.OrderItemRequest(productId1, 1));
            OrderV1Dto.OrderCreateRequest request = new OrderV1Dto.OrderCreateRequest(
                    items,
                    "ORDER-SEQ-123",
                    -1L
            );

            // when & then
            mockMvc.perform(get("/api/v1/orders")
                            .header("X-USER-ID", "")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
