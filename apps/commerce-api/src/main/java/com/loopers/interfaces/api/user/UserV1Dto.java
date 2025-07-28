package com.loopers.interfaces.api.user;

import com.loopers.application.user.in.UserRegisterCommand;
import com.loopers.application.user.out.UserInfo;
import jakarta.validation.constraints.NotBlank;

public class UserV1Dto {
    public record UserRegisterRequest(
            @NotBlank(message = "아이디는 필수입니다.")
            String loginId,
            @NotBlank(message = "이메일은 필수입니다.")
            String email,
            @NotBlank(message = "생년월일은 필수입니다.")
            String birthDate,
            @NotBlank(message = "성별은 필수입니다.")
            String gender
    ){
        public static UserRegisterCommand toCommand(UserRegisterRequest request) {
            return new UserRegisterCommand(
                    request.loginId(),
                    request.email(),
                    request.birthDate(),
                    request.gender()
            );
        }
    }
    public record UserInfoResponse(
            Long id,
            String loginId,
            String email,
            String birthDate,
            String gender
    ){
        public static UserInfoResponse from(UserInfo userInfo) {
            return new UserInfoResponse(
                    userInfo.id(),
                    userInfo.loginId(),
                    userInfo.email(),
                    userInfo.birthDate(),
                    userInfo.gender()
            );
        }
    }
}
