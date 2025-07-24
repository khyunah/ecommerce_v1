package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    boolean existsByUserId(String id);
    Optional<User> save(User user);
}
