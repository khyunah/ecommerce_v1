package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.model.LoginId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByLoginId(LoginId loginId) {
        return userJpaRepository.findByLoginId(loginId).isPresent();
    }

    @Override
    public Optional<User> save(User user) {
        return Optional.of(userJpaRepository.save(user));
    }

    @Override
    public Optional<User> findByLoginId(LoginId loginId) {
        return userJpaRepository.findByLoginId(loginId);
    }

}
