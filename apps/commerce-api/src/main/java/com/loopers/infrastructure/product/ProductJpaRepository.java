package com.loopers.infrastructure.product;

import com.loopers.domain.point.Point;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product,Long> {
    Optional<Point> findByRefUserId(UserId refUserId);
}
