package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@SpringBootTest
@Transactional
@Slf4j
class UserServiceIntegrationTest {

    private UserService userSpyService;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        UserService realService = new UserService(userRepository);
        userSpyService = Mockito.spy(realService);
    }

    @DisplayName("회원 가입시 User 저장이 수행된다.")
    @Test
    void savingUser_whenSuccessToJoin() {
        // given
        User user = User.from("loginID", "asd123@gmail.com", "2000-03-12", "F");

        // when
        User savedUser = userSpyService.register(user);
        // then
        assertAll(
                () -> assertThat(savedUser).isNotNull(),
                () -> assertThat(savedUser.getUserId()).isEqualTo(user.getUserId()),
                () -> assertThat(savedUser.getEmail()).isEqualTo(user.getEmail()),
                () -> assertThat(savedUser.getBirthDate()).isEqualTo(user.getBirthDate()),
                () -> assertThat(savedUser.getGender()).isEqualTo(user.getGender())
        );

        Mockito.verify(userSpyService, times(1)).register(any(User.class));
    }

    @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
    @Test
    void failToJoin_whenAlreadyUserLoginId() {
        // given
        String loginId = "testId123";
        User firstUser = User.from(loginId, "asd123@gmail.com", "2000-03-12", "F");
        userSpyService.register(firstUser);
        User secondUser = User.from(loginId, "qwer000@naver.com", "1999-12-01", "M");

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            userSpyService.register(secondUser);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);

    }

    @DisplayName("해당 userId 의 회원이 존재할 경우, 회원 정보가 반환된다.")
    @Test
    void returnUserInfo_whenLoginIdExists() {
        // given
        User firstUser = User.from("testId123", "asd123@gmail.com", "2000-03-12", "F");
        User saveUser = userSpyService.register(firstUser);

        // when
        User selectUser = userSpyService.getByLoginId(saveUser.getUserId().getValue());

        log.info("selectUser={}", selectUser.getEmail());

        // then
        assertAll(
                () -> assertThat(selectUser).isNotNull(),
                () -> assertThat(selectUser.getId()).isEqualTo(saveUser.getId()),
                () -> assertThat(selectUser.getUserId()).isEqualTo(saveUser.getUserId()),
                () -> assertThat(selectUser.getEmail()).isEqualTo(saveUser.getEmail()),
                () -> assertThat(selectUser.getBirthDate()).isEqualTo(saveUser.getBirthDate()),
                () -> assertThat(selectUser.getGender()).isEqualTo(saveUser.getGender())
        );
    }

    @DisplayName("해당 userId 의 회원이 존재하지 않을 경우, null 이 반환된다.")
    @Test
    void returnNull_whenNotFoundId() {
        // given
        String userId = "test123";

        // when
        User result = userSpyService.getByLoginId(userId);

        // then
        assertThat(result).isNull();
    }
}
