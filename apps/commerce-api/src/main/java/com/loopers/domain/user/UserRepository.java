package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByUserId(String id);
    boolean existsByUserId(String id);
    Optional<User> save(User user);
}
