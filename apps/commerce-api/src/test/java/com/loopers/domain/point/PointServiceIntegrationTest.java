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
        Point point = new Point("test124", 10000);
        Point save = pointRepository.save(point);

        // when
        Point selectPoint = pointSpyService.get(point.getUserId());
        int savedPoint = save.getPoint();

        // then
        assertThat(savedPoint).isEqualTo(selectPoint.getPoint());
    }

    @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
    @Test
    void returnNull_whenUserIdNotFound() {
        // given
        String userId = "test1212";
        // when
        Point point = pointSpyService.get(userId);

        // then
        assertThat(point).isNull();
    }

    @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
    @Test
    void failsToCharge_whenUserIdDoesNotExist() {
        // given
        Point point = new Point("test124", 10000);
        int amount = 1000;
        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            pointSpyService.charge(point, amount);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 유저 ID 로 충전을 시도했습니다.");

    }

}
