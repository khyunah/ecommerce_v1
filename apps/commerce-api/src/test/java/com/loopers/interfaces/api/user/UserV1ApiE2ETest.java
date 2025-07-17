package com.loopers.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
            UserV1Dto.UserJoinRequest request = new UserV1Dto.UserJoinRequest(
                    "asd123",
                    "test123@naver.com",
                    "1994-03-15",
                    "FEMALE"
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
            UserV1Dto.UserJoinRequest request = new UserV1Dto.UserJoinRequest(
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

}
