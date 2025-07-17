package com.loopers.application.user;

import com.loopers.domain.user.User;

public record UserInfo(
        Long id,
        String userId,
        String email,
        String birthDate,
        String gender) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getUserId(),
                user.getEmail(),
                user.getBirthDate().toString(),
                user.getGender().name()
        );
    }

}
