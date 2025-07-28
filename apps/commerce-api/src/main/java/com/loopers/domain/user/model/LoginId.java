package com.loopers.domain.user.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Pattern;

@Getter
@ToString
@Embeddable
public class LoginId {
    private static final Pattern ID_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{1,10}$");

    @Column(name = "loginId")
    private String value;

    public LoginId() {}

    private LoginId(String value) {
        this.value = value;
    }

    public static LoginId from(String value) {
        validate(value);
        return new LoginId(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "ID는 null이거나 빈 문자열일 수 없습니다.");
        } else if (!ID_PATTERN.matcher(value).matches()){
            throw new CoreException(ErrorType.BAD_REQUEST, "ID 가 영문 및 숫자 10자 이내 형식에 맞지 않습니다.");
        }
    }
}
