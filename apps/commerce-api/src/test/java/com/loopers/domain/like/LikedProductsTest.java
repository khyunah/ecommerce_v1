package com.loopers.domain.like;

import com.loopers.domain.like.dto.LikedProductDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LikedProductsTest {
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @DisplayName("좋아요 한 상품 목록을 조회한다.")
    @Test
    void returnLikedProducts_whenUserHasLikes() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        LikedProductDto dto = new LikedProductDto(
                100L,
                200L,
                "상품명",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(9000),
                "SELLING",
                true,
                5L
        );

        Page<LikedProductDto> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(likeRepository.findLikedProductsByRefUserId(userId, pageable)).thenReturn(page);

        Page<LikedProductDto> result = likeService.getLikedProducts(userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).productName()).isEqualTo("상품명");

        verify(likeRepository).findLikedProductsByRefUserId(userId, pageable);
    }
}
