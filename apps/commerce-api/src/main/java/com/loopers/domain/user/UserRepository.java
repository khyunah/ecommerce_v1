package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    boolean existsByUserId(String id);
    Optional<User> save(User user);
}
