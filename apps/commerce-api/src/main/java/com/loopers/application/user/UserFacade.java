package com.loopers.application.user;

import com.loopers.application.user.in.UserRegisterCommand;
import com.loopers.application.user.out.UserInfo;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo register(UserRegisterCommand command) {
        User user = UserRegisterCommand.toDomain(command);
        return UserInfo.from(userService.register(user));
    }

    public UserInfo get(Long id){
        User user = userService.get(id);
        return UserInfo.from(user);
    }


}
