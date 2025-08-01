package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class PointServiceIntegrationTest {
    private PointService pointSpyService;
    @Autowired
    private PointRepository pointRepository;

    @BeforeEach
    void setUp() {
        PointService realService = new PointService(pointRepository);
        pointSpyService = Mockito.spy(realService);
    }

    @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
    @Test
    void returnUserInfo_whenIdExists() {
        // given
        String userId = "test123";
        Point point = Point.from(userId, 10000L);
        Point savedPoint = pointRepository.save(point);

        // when
        Point selectPoint = pointSpyService.get(userId);

        // then
        assertThat(savedPoint.getBalance()).isEqualTo(selectPoint.getBalance());
    }

    @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
    @Test
    void returnNull_whenUserIdNotFound() {
        // given
        String refUserId = "test123";

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            pointSpyService.get(refUserId);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 유저 ID 입니다.");
    }

    @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
    @Test
    void failsToCharge_whenUserIdDoesNotExist() {
        // given
        Point point = Point.from("test123", 10000L);
        Long amount = 1000L;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            pointSpyService.charge(point, amount);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 유저 ID 로 충전을 시도했습니다.");

    }

}
