package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Entity
@Getter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String userId;
    @Column(nullable = false, unique = true)
    private String email;
    private LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    private Gender gender;

    public enum Gender{
        MALE,FEMALE,OTHER;
    }

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{1,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    public User(){}
    public User(String userId, String email, LocalDate birthDate, Gender gender) {
        validateUserId(userId);
        validateEmail(email);
        validateBirthDate(birthDate.toString());
        this.userId =  userId;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static void validateUserId(String userId){
        if (!ID_PATTERN.matcher(userId).matches()){
            throw new CoreException(ErrorType.BAD_REQUEST, "ID 가 영문 및 숫자 10자 이내 형식에 맞지 않습니다.");
        }
    }

    public static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()){
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일이 xx@yy.zz 형식에 맞지 않습니다.");
        }
    }

    public static void validateBirthDate(String birthDate) {
        try {
            LocalDate.parse(birthDate);
        } catch (Exception e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일이 YYYY-MM-DD 형식에 맞지 않습니다.");
        }
    }
}
