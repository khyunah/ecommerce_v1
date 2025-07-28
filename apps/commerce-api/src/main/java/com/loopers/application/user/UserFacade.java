package com.loopers.application.user;

import com.loopers.application.user.in.UserRegisterCommand;
import com.loopers.application.user.out.UserInfo;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.model.Gender;
import com.loopers.interfaces.api.user.UserV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo register(UserRegisterCommand command) {
        User user = UserRegisterCommand.toDomain(command);
        return UserInfo.from(userService.register(user));
    }

    public UserInfo get(String loginId){
        User user = userService.getByLoginId(loginId);
        return UserInfo.from(user);
    }
}
