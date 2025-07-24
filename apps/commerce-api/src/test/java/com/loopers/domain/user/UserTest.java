package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @Test
    void failToCreate_whenUnValidateUserId() {
        // given
        String userId = "abcd123456789";

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
           User.validateUserId(userId);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않습니다.");
    }

    @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @Test
    void failToCreate_whenUnValidateEmail() {
        // given
        String email = "aa.gmail.com";

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
           User.validateEmail(email);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("이메일이 xx@yy.zz 형식에 맞지 않습니다.");
    }

    @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @Test
    void failToCreate_whenUnValidateBirthDate() {
        // given
        String birthDate = "200-09-09";

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
           User.validateBirthDate(birthDate);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("생년월일이 YYYY-MM-DD 형식에 맞지 않습니다.");
    }
}
