package com.loopers.domain.user;

import com.loopers.domain.user.vo.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User register(User user) {
        if(userRepository.existsByUserId(user.getUserId())){
            throw new CoreException(ErrorType.CONFLICT, "이미 가입된 ID 입니다.");
        };

        return userRepository.save(user)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "저장할 수 없습니다."));
    }

    public User getByLoginId(String loginId) {
        return userRepository.findByUserId(UserId.from(loginId))
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 아이디입니다."));
    }

}
