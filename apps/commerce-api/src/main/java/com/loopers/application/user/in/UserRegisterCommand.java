package com.loopers.application.user.in;

import com.loopers.domain.user.User;

public record UserRegisterCommand(
        String loginId,
        String email,
        String birthDate,
        String gender
) {
    public static User toDomain(UserRegisterCommand command) {
        return User.from(
                command.loginId,
                command.email,
                command.birthDate,
                command.gender
        );
    }
}
