package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@SpringBootTest
@Transactional
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
        User user = new User("userID", "asd123@gmail.com", LocalDate.parse("2000-03-12"), User.Gender.FEMALE);

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
    void failToJoin_whenAlreadyUserId() {
        // given
        String userID = "testId123";
        User firstUser = new User(userID, "asd123@gmail.com", LocalDate.parse("2000-03-12"), User.Gender.FEMALE);
        userSpyService.register(firstUser);
        User secondUser = new User(userID, "qwer000@naver.com", LocalDate.parse("1999-12-01"), User.Gender.MALE);

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            userSpyService.register(secondUser);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);

    }

}
