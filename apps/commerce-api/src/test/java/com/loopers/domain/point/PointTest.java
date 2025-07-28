package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointTest {

    @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
    @Test
    void failToCreate_whenUnValidateUserId() {
        // given
        int point = 0;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            Point.validatePoint(point);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("0 이하의 정수로 포인트를 충전할 수 없습니다.");
    }
}
