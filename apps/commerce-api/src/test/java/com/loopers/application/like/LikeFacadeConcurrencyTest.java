package com.loopers.application.like;

import com.loopers.application.like.in.LikeActionCommand;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LikeFacadeConcurrencyTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LikeRepository likeRepository;

    private final int threadCount = 10;

    private Long userId1;
    private Long userId2;
    private Long productId1;
    private Long productId2;

    @BeforeEach
    void setUp() {
        // 유저 저장
        User user1 = User.from("testUser01", "test1@example.com", "1990-01-01", "M");
        user1 = userRepository.save(user1).orElseThrow();
        userId1 = user1.getId();

        User user2 = User.from("testUser02", "test2@example.com", "2000-11-24", "F");
        user2 = userRepository.save(user2).orElseThrow();
        userId2 = user2.getId();

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

        Product product2 = Product.from(
                "티셔츠",
                "티셔츠 상품 설명",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(15000),
                "ON_SALE",
                1L
        );
        product2 = productRepository.save(product2);
        productId2 = product2.getId();
    }

    @DisplayName("여러 유저가 동일한 상품에 동시에 좋아요를 누를 때, 좋아요 수가 정확하게 증가한다.")
    @Test
    void should_increase_like_count_correctly_when_multiple_users_like_same_product_concurrently() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 초기 좋아요 수 확인
        Product initialProduct = productService.getDetail(productId1);
        Long initialLikeCount = initialProduct.getLikeCount();

        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    // 각 스레드마다 다른 유저로 좋아요 생성
                    Long currentUserId = (userIndex % 2 == 0) ? userId1 : userId2;
                    LikeActionCommand command = new LikeActionCommand(currentUserId, productId1);
                    likeFacade.create(command);
                } catch (Exception e) {
                    // 예외 무시 (중복 좋아요 등으로 인한 충돌 가능)
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 검증
        Product finalProduct = productService.getDetail(productId1);
        List<Like> likes = likeRepository.findAllByProductId(productId1);

        System.out.println("초기 좋아요 수 = " + initialLikeCount);
        System.out.println("최종 좋아요 수 = " + finalProduct.getLikeCount());
        System.out.println("실제 Like 레코드 수 = " + likes.size());

        // 좋아요 수와 실제 레코드 수가 일치해야 함
        assertThat(finalProduct.getLikeCount()).isEqualTo(initialLikeCount + likes.size());
        assertThat(likes.size()).isLessThanOrEqualTo(threadCount);
    }

    @DisplayName("동일한 유저가 같은 상품에 동시에 좋아요/좋아요 취소를 수행할 때, 데이터 일관성이 유지된다.")
    @Test
    void should_maintain_data_consistency_when_same_user_likes_and_unlikes_same_product_concurrently() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 초기 좋아요 생성
        LikeActionCommand initialLikeCommand = new LikeActionCommand(userId1, productId1);
        likeFacade.create(initialLikeCommand);

        Product initialProduct = productService.getDetail(productId1);
        Long initialLikeCount = initialProduct.getLikeCount();

        for (int i = 0; i < threadCount; i++) {
            final int actionIndex = i;
            executorService.submit(() -> {
                try {
                    LikeActionCommand command = new LikeActionCommand(userId1, productId1);

                    // 홀수는 좋아요, 짝수는 좋아요 취소
                    if (actionIndex % 2 == 0) {
                        likeFacade.create(command);
                    } else {
                        likeFacade.delete(command);
                    }
                } catch (Exception e) {
                    // 예외 무시 (중복 좋아요나 이미 삭제된 좋아요 등)
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 검증
        Product finalProduct = productService.getDetail(productId1);
        List<Like> likes = likeRepository.findAllByUserIdAndProductId(userId1, productId1);

        System.out.println("초기 좋아요 수 = " + initialLikeCount);
        System.out.println("최종 좋아요 수 = " + finalProduct.getLikeCount());
        System.out.println("해당 유저의 Like 레코드 수 = " + likes.size());

        // 좋아요 수와 실제 레코드 존재 여부가 일치해야 함
        if (likes.isEmpty()) {
            // 좋아요가 없으면 초기값보다 1 감소해야 함
            assertThat(finalProduct.getLikeCount()).isEqualTo(initialLikeCount - 1);
        } else {
            // 좋아요가 있으면 초기값과 같아야 함
            assertThat(finalProduct.getLikeCount()).isEqualTo(initialLikeCount);
            assertThat(likes.size()).isEqualTo(1);
        }
    }

    @DisplayName("여러 상품에 대해 동시에 좋아요 작업을 수행할 때, 각 상품의 좋아요 수가 정확하게 계산된다.")
    @Test
    void should_calculate_like_count_correctly_when_liking_multiple_products_concurrently() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int actionIndex = i;
            executorService.submit(() -> {
                try {
                    // 홀수는 상품1, 짝수는 상품2에 좋아요
                    Long targetProductId = (actionIndex % 2 == 0) ? productId1 : productId2;
                    Long targetUserId = (actionIndex % 4 < 2) ? userId1 : userId2;

                    LikeActionCommand command = new LikeActionCommand(targetUserId, targetProductId);
                    likeFacade.create(command);
                } catch (Exception e) {
                    // 예외 무시
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 검증
        Product product1 = productService.getDetail(productId1);
        Product product2 = productService.getDetail(productId2);
        List<Like> likesForProduct1 = likeRepository.findAllByProductId(productId1);
        List<Like> likesForProduct2 = likeRepository.findAllByProductId(productId2);

        System.out.println("상품1 좋아요 수 = " + product1.getLikeCount());
        System.out.println("상품1 실제 Like 레코드 수 = " + likesForProduct1.size());
        System.out.println("상품2 좋아요 수 = " + product2.getLikeCount());
        System.out.println("상품2 실제 Like 레코드 수 = " + likesForProduct2.size());

        // 각 상품의 좋아요 수와 실제 레코드 수가 일치해야 함
        assertThat(product1.getLikeCount()).isEqualTo(likesForProduct1.size());
        assertThat(product2.getLikeCount()).isEqualTo(likesForProduct2.size());
        assertThat(likesForProduct1.size() + likesForProduct2.size()).isLessThanOrEqualTo(threadCount);
    }
}
