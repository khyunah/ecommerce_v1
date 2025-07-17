package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;

public class UserV1Dto {
    public record UserJoinRequest(
            String userId,
            String email,
            String birthDate,
            String gender
    ){}
    public record UserInfoResponse(
            Long id,
            String userId,
            String email,
            String birthDate,
            String gender
    ){
        public static UserInfoResponse from(UserInfo userInfo) {
            return new UserInfoResponse(
                    userInfo.id(),
                    userInfo.userId(),
                    userInfo.email(),
                    userInfo.birthDate(),
                    userInfo.gender()
            );
        }
    }
}
