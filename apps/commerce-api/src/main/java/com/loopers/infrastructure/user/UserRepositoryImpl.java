package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByUserId(UserId userId) {
        return userJpaRepository.findByUserId(userId).isPresent();
    }

    @Override
    public Optional<User> save(User user) {
        return Optional.of(userJpaRepository.save(user));
    }

    @Override
    public Optional<User> findByUserId(UserId userId) {
        return userJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

}
