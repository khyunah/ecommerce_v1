package com.loopers.interfaces.api.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PointV1ApiE2ETest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private PointService pointService;


    @DisplayName("/api/v1/points")
    @Nested
    class Get {

        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenGetMyInfoSuccess() throws Exception {
            // given
            String userId = "asd123";
            int point = 10000;
            Point savePoint = pointRepository.save(new Point(userId,point));

            // when & then
            mockMvc.perform(get("/api/v1/points/"+savePoint.getUserId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(savePoint.getUserId()))
                    .andExpect(jsonPath("$.point").value(savePoint.getPoint()))
                    .andDo(print());
        }

        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returns400BadRequest_whenIsNullGender() throws Exception {
            // given
            String userId = "asd123";
            int amount = 1000;
            PointV1Dto.PointChargeRequest request = new PointV1Dto.PointChargeRequest(userId,amount);

            // when & then
            mockMvc.perform(post("/api/v1/points/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @DisplayName("/api/v1/points/charge")
    @Nested
    class charge {

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsTotalPoint_whenUserExistsAndCharges1000Won() throws Exception {
            // given
            Point point = new Point("asd123",10000);
            pointRepository.save(point);
            PointV1Dto.PointChargeRequest request = new PointV1Dto.PointChargeRequest(
                    point.getUserId(),
                    1000
            );
            // when & then
            mockMvc.perform(post("/api/v1/points/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(request.userId()))
                    .andExpect(jsonPath("$.point").value(request.amount() + point.getPoint()))
                    .andDo(print());
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returns400BadRequest_whenIsNullGender() throws Exception {
            // given
            String userId = "asd123";
            int amount = 1000;
            PointV1Dto.PointChargeRequest request = new PointV1Dto.PointChargeRequest(userId,amount);

            // when & then
            mockMvc.perform(post("/api/v1/points/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
