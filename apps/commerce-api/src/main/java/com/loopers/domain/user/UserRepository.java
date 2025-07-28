package com.loopers.domain.user;

import com.loopers.domain.user.model.LoginId;

import java.util.Optional;

public interface UserRepository {
    boolean existsByLoginId(LoginId id);
    Optional<User> save(User user);
    Optional<User> findByLoginId(LoginId loginId);
}
