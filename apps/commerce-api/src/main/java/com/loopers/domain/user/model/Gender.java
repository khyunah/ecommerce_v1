package com.loopers.domain.user.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Arrays;

public enum Gender {
    M,F;

    public static Gender from(String value) {
        validate(value);
        return Gender.valueOf(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 null이거나 빈 문자열일 수 없습니다.");
        } else {
            boolean exists = Arrays.stream(Gender.values())
                    .anyMatch(g -> g.name().equalsIgnoreCase(value));

            if (!exists) {
                throw new CoreException(ErrorType.BAD_REQUEST, "성별은 'M' 또는 'F' 여야 합니다.");
            }
        }

    }
}
