package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock,Long> {
    Optional<Stock> findByRefProductId(Long refProductId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.refProductId = :refProductId")
    Optional<Stock> findByRefProductIdWithLock(Long refProductId);
}
