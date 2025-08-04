package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeJpaRepository extends JpaRepository<Like,Long> {
    boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
    boolean deleteByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
    Page<Like> findAllByRefUserId(Long refUserId, Pageable pageable);
}
