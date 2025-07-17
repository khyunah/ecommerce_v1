package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByUserId(String id) {
        return userJpaRepository.findByUserId(id).isPresent();
    }

    @Override
    public Optional<User> save(User user) {
        return Optional.of(userJpaRepository.save(user));
    }
}
