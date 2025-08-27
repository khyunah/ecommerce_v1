package com.loopers.application.like;

import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LikeFacadeEventualConsistencyTest {
    
    // 의존성 주입 필요...
    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long userId1;
    private Long productId1;

    @BeforeEach
    void setUp() {
        // 유저 저장
        User user1 = User.from("testUser01", "test1@example.com", "1990-01-01", "M");
        user1 = userRepository.save(user1).orElseThrow();
        userId1 = user1.getId();

        // 상품 저장
        Product product1 = Product.from(
                "청바지",
                "청바지 상품 설명",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                "ON_SALE",
                1L
        );
        product1 = productRepository.save(product1);
        productId1 = product1.getId();

    }
    
    @DisplayName("좋아요 생성은 즉시 성공하고, 집계는 비동기로 처리된다.")
    @Test  
    void should_create_like_immediately_and_update_count_asynchronously() {
        // Given
        LikeActionCommand command = new LikeActionCommand(userId1, productId1);
        
        // When - 좋아요 생성
        boolean result = likeFacade.create(command);
        
        // Then - 좋아요는 즉시 성공
        assertThat(result).isTrue();
        
        // 좋아요 레코드는 즉시 생성됨
        List<Like> likes = likeRepository.findAllByProductId(productId1);
        assertThat(likes).hasSize(1);
        
        // 집계는 비동기로 처리되므로 eventual consistency
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                Product product = productService.getDetail(productId1);
                assertThat(product.getLikeCount()).isEqualTo(1L);
            });
    }
}
