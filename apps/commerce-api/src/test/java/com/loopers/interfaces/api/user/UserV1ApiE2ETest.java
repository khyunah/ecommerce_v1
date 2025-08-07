package com.loopers.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.user.UserJpaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserV1ApiE2ETest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private UserJpaRepository userRepository;
    @Autowired
    private UserService userService;

    @DisplayName("POST /api/v1/users")
    @Nested
    class Join {

        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenSuccessToJoin() throws Exception {
            // given
            UserV1Dto.UserRegisterRequest request = new UserV1Dto.UserRegisterRequest(
                    "asd123",
                    "test123@naver.com",
                    "1994-03-15",
                    "F"
            );
            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(request.userId()))
                    .andExpect(jsonPath("$.email").value(request.email()))
                    .andExpect(jsonPath("$.birthDate").value(request.birthDate()))
                    .andExpect(jsonPath("$.gender").value(request.gender()))
                    .andDo(print());
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returns400BadRequest_whenIsNullGender() throws Exception {
            // given
            UserV1Dto.UserRegisterRequest request = new UserV1Dto.UserRegisterRequest(
                    "asd123",
                    "test123@naver.com",
                    "1994-03-15",
                    ""
            );
            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @DisplayName("Get /api/v1/users/me")
    @Nested
    class Get {

        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenGetMyInfoSuccess() throws Exception {
            // given
            String userId = "asd123";
            String email = "aa@naver.com";
            String birthDate = "1994-03-15";
            User user = userService.register(User.from(userId,email, birthDate, "F"));

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("X-USER-ID", user.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(user.getId()))
                    .andExpect(jsonPath("$.userId").value(user.getUserId().getValue()))
                    .andExpect(jsonPath("$.email").value(user.getEmail().getValue()))
                    .andExpect(jsonPath("$.birthDate").value(user.getBirthDate().getValue()))
                    .andExpect(jsonPath("$.gender").value(user.getGender().name()))
                    .andDo(print());
        }

        @DisplayName("존재하지 않는 userId 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returns404NotFound_whenUserIdDoesNotExist() throws Exception {
            // given
            Long userId = 240L;
            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("X-USER-ID", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
