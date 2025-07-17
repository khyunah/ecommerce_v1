package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.user.UserV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo register(UserV1Dto.UserJoinRequest request) {
        User requestUser = new User(
                request.userId(),
                request.email(),
                LocalDate.parse(request.birthDate()),
                User.Gender.valueOf(request.gender())
        );
        return UserInfo.from(userService.register(requestUser));
    }

}
