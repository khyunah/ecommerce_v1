package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.Stock;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock,Long> {
    Optional<Stock> findByRefProductId(Long refProductId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.refProductId = :refProductId")
    @QueryHints(value = {@QueryHint(name = "javax.persistence.lock.timeout", value = "5000")})
    Optional<Stock> findByRefProductIdWithLock(Long refProductId);
}
