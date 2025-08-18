package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSortTypeTest {

    @Test
    @DisplayName("유효한 문자열로 ProductSortType 변환 성공 - LATEST")
    void fromString_validLatest() {
        // given & when & then
        assertThat(ProductSortType.fromString("LATEST")).isEqualTo(ProductSortType.LATEST);
        assertThat(ProductSortType.fromString("latest")).isEqualTo(ProductSortType.LATEST);
        assertThat(ProductSortType.fromString("Latest")).isEqualTo(ProductSortType.LATEST);
    }

    @Test
    @DisplayName("유효한 문자열로 ProductSortType 변환 성공 - LIKE_COUNT")
    void fromString_validLikeCount() {
        // given & when & then
        assertThat(ProductSortType.fromString("LIKE_COUNT")).isEqualTo(ProductSortType.LIKE_COUNT);
        assertThat(ProductSortType.fromString("like_count")).isEqualTo(ProductSortType.LIKE_COUNT);
        assertThat(ProductSortType.fromString("Like_Count")).isEqualTo(ProductSortType.LIKE_COUNT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("null이나 빈 문자열인 경우 기본값 LATEST 반환")
    void fromString_nullOrBlank_returnsDefault(String input) {
        // given & when & then
        assertThat(ProductSortType.fromString(input)).isEqualTo(ProductSortType.LATEST);
    }

    @Test
    @DisplayName("null인 경우 기본값 LATEST 반환")
    void fromString_null_returnsDefault() {
        // given & when & then
        assertThat(ProductSortType.fromString(null)).isEqualTo(ProductSortType.LATEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "POPULARITY", "DATE", "123", "LIKE", "COUNT"})
    @DisplayName("유효하지 않은 문자열인 경우 CoreException 발생")
    void fromString_invalid_throwsException(String invalidInput) {
        // given & when & then
        assertThatThrownBy(() -> ProductSortType.fromString(invalidInput))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST)
                .hasMessageContaining("유효하지 않은 정렬 타입입니다");
    }

    @Test
    @DisplayName("유효하지 않은 문자열 예외 메시지 확인")
    void fromString_invalid_correctErrorMessage() {
        // given
        String invalidInput = "WRONG_TYPE";

        // when & then
        assertThatThrownBy(() -> ProductSortType.fromString(invalidInput))
                .isInstanceOf(CoreException.class)
                .hasMessage("유효하지 않은 정렬 타입입니다. (LATEST, LIKE_COUNT 중 하나를 입력해주세요)");
    }
}
