package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
@ToString
@Embeddable
public class UserId {
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{1,10}$");

    @Column(name = "userId")
    private String value;

    public UserId() {}

    private UserId(String value) {
        this.value = value;
    }

    public static UserId from(String value) {
        validate(value);
        return new UserId(value);
    }

    public static void validate(String value){
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "ID는 null이거나 빈 문자열일 수 없습니다.");
        } else if (!ID_PATTERN.matcher(value).matches()){
            throw new CoreException(ErrorType.BAD_REQUEST, "ID 가 영문 및 숫자 10자 이내 형식에 맞지 않습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
