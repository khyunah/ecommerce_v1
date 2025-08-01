package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandTest {

    @DisplayName("브랜드명은 null이거나 빈 문자열일 경우 400 BAD_REQUEST 에러를 반환한다.")
    @Test
    void failToCreateBrand_whenBrandNameIsNullOrBlank() {
        // given
        String name = "";

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Brand.validate(name);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("브랜드명은 null이거나 빈 문자열일 수 없습니다.");
    }
}
