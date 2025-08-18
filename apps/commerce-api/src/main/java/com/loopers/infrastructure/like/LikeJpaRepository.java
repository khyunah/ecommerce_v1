package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeJpaRepository extends JpaRepository<Like,Long> {
    boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
    int deleteByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
    Page<Like> findAllByRefUserId(Long refUserId, Pageable pageable);

    List<Like> findAllByRefProductId(Long refProductId);

    List<Like> findAllByRefUserIdAndRefProductId(Long refUserId, Long refProductId);
}
