package com.loopers.domain.user.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Getter
@ToString
@Embeddable
public class BirthDate {

    @Column(name = "birthDate")
    private String value;

    public BirthDate() {}

    private BirthDate(String value) {
        this.value = value;
    }

    public static BirthDate from(String value) {
        validate(value);
        return new BirthDate(value);
    }

    public static void validate(String value) {
        try {
            LocalDate.parse(value);
        } catch (Exception e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일이 YYYY-MM-DD 형식에 맞지 않습니다.");
        }
    }

}
