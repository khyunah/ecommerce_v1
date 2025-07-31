package com.loopers.domain.like;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class LikeServiceIntegrationTest {

    private LikeService likeService;
    private StubLikeRepository stubLikeRepository;

    @BeforeEach
    void setUp() {
        stubLikeRepository = new StubLikeRepository();
        likeService = new LikeService(stubLikeRepository);
    }

    @DisplayName("좋아요 등록 요청 시, 처음이면 저장되고 true가 반환된다.")
    @Test
    void returnTrue_whenLikeIsSavedFirstTime() {
        // given
        Like like = new Like(1L, 100L); // userId=1, productId=100

        // when
        boolean result = likeService.create(like);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("좋아요 등록 요청 시, 멱등성 보장이 되도록 이미 존재하면 아무 작업 없이 true가 반환된다.")
    @Test
    void returnTrue_whenLikeAlreadyExists() {
        // given
        Like like = new Like(1L, 100L);
        stubLikeRepository.save(like);

        // when
        boolean result = likeService.create(like);

        // then
        assertThat(result).isTrue();
    }

}

class StubLikeRepository implements LikeRepository {
    // Map<refUserId, Set<refProductId>>
    private final Map<Long, Set<Long>> store = new HashMap<>();

    @Override
    public Like save(Like like) {
        Long refUserId = like.getRefUserId();
        Long refProductId = like.getRefProductId();

        store.computeIfAbsent(refUserId, k -> new HashSet<>());
        store.get(refUserId).add(refProductId);
        return like;
    }

    @Override
    public boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId) {
        return store.getOrDefault(refUserId, Collections.emptySet()).contains(refProductId);
    }

    @Override
    public boolean delete(Long refUserId, Long refProductId) {
        Set<Long> productSet = store.get(refUserId);
        if (productSet == null) return false;
        boolean removed = productSet.remove(refProductId);
        if (productSet.isEmpty()) {
            store.remove(refUserId);
        }
        return removed;
    }
}
