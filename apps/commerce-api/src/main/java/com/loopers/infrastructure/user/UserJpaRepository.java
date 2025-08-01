package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User,Long> {
    Optional<User> findByUserId(UserId userId);
}
