package com.loopers.domain.like;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class LikeServiceIntegrationTest {

    private LikeService likeSpyService;
    @Autowired
    private LikeRepository likeRepository;

    @BeforeEach
    void setUp() {
        LikeService realService = new LikeService(likeRepository);
        likeSpyService = Mockito.spy(realService);
    }

    @DisplayName("좋아요 등록 요청 시, 처음이면 저장되고 true가 반환된다.")
    @Test
    void returnTrue_whenLikeIsSavedFirstTime() {
        // given
        Like like = new Like(1L, 101L);

        // when
        boolean result = likeSpyService.create(like);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("좋아요 등록 요청 시, 멱등성 보장이 되도록 이미 존재하면 IllegalArgumentException 에러가 반환된다.")
    @Test
    void returnTrue_whenLikeAlreadyExists() {
        // given
        Like like = new Like(1L, 100L);
        likeRepository.save(like);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            likeSpyService.create(like);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("좋아요는 하나의 상품에 하나만 가능합니다.");
    }

}
