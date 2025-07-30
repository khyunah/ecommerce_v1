package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeJpaRepository extends JpaRepository<Like,Long> {
    boolean save(Long refUserId, Long refProductId);
    boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId);

    boolean deleteByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
}
