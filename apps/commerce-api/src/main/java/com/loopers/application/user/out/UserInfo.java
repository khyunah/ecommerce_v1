package com.loopers.application.user.out;

import com.loopers.domain.user.User;

public record UserInfo(
        Long id,
        String loginId,
        String email,
        String birthDate,
        String gender) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getLoginId().getValue(),
                user.getEmail().getValue(),
                user.getBirthDate().getValue(),
                user.getGender().name()
        );
    }

}
