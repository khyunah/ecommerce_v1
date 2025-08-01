package com.loopers.support.auth;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;

public class AuthenticatedUserIdProvider {
    private static final String USER_ID_HEADER = "X-USER-ID";

    public static Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 존재하지 않습니다.");
        }

        return Long.valueOf(userId);
    }
}
