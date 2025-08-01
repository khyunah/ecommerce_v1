package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LikeTest {

    @DisplayName("참조 User ID가 null일 경우, 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreate_whenRefUserIdIsNull(){
        // given
        Long refUserId = null;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Like.validateRefUserId(refUserId);
        });

        // than
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("참조 사용자 ID는 null일 수 없습니다.");
    }

    @DisplayName("참조 상품 ID가 null일 경우, 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreate_whenRefProductIdIsNull(){
        // given
        Long refProductId = null;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Like.validateRefProductId(refProductId);
        });

        // than
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("참조 상품 ID는 null일 수 없습니다.");
    }

    @DisplayName("참조 사용자 ID가 음수일 경우, 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreate_whenReferenceUserIdIsNegative(){
        // given
        Long refUserId = -1L;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Like.validateRefUserId(refUserId);
        });

        // than
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("참조 사용자 ID는 음수일 수 없습니다.");
    }

    @DisplayName("참조 상품 ID가 음수일 경우, 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreate_whenReferenceProductIdIsNegative(){
        // given
        Long refProductId = -1L;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Like.validateRefProductId(refProductId);
        });

        // than
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("참조 상품 ID는 음수일 수 없습니다.");
    }

}
