package com.loopers.domain.user;

import com.loopers.domain.user.vo.UserId;

import java.util.Optional;

public interface UserRepository {
    boolean existsByUserId(UserId id);
    Optional<User> save(User user);
    Optional<User> findByUserId(UserId userId);
}
