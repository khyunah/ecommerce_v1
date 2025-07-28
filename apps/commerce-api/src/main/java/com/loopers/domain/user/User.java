package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.Gender;
import com.loopers.domain.user.vo.UserId;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "user")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UserId userId;

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

    public User(UserId userId, Email email, BirthDate birthDate, Gender gender) {
        this.userId = userId;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static User from(String userId, String email, String birthDate, String gender) {
        return new User(
                UserId.from(userId),
                Email.from(email),
                BirthDate.from(birthDate),
                Gender.from(gender)
        );
    }

}
