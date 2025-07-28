package com.loopers.domain.user;

import com.loopers.domain.user.model.BirthDate;
import com.loopers.domain.user.model.Email;
import com.loopers.domain.user.model.Gender;
import com.loopers.domain.user.model.LoginId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.regex.Pattern;

@Entity
@Getter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LoginId loginId;

    @Embedded
    @Column(nullable = false, unique = true)
    private Email email;

    @Embedded
    private BirthDate birthDate;

    @Embedded
    @Enumerated(EnumType.STRING)
    private Gender gender;

    public User() {
    }

    public User(LoginId loginId, Email email, BirthDate birthDate, Gender gender) {
        this.loginId = loginId;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static User from(String loginId, String email, String birthDate, String gender) {
        return new User(
                LoginId.from(loginId),
                Email.from(email),
                BirthDate.from(birthDate),
                Gender.from(gender)
        );
    }

}
