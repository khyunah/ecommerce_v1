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
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    @Column(name = "email")
    private String value;

    public Email() {}

    private Email(String value) {
        this.value = value;
    }

    public static Email from(String value) {
        validate(value);
        return new Email(value);
    }

    public static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 null이거나 빈 문자열일 수 없습니다.");
        } else if (!EMAIL_PATTERN.matcher(value).matches()){
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일이 xx@yy.zz 형식에 맞지 않습니다.");
        }
    }

}
