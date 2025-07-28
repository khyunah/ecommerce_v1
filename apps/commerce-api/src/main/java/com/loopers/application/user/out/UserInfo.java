package com.loopers.application.user.out;

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
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getBirthDate().getValue(),
                user.getGender().name()
        );
    }

}
