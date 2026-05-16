package com.kshitij.trading.domain.repository;

import com.kshitij.trading.domain.entity.Portfolio;
import com.kshitij.trading.domain.entity.Stock;
import com.kshitij.trading.domain.entity.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Portfolio p WHERE p.trader = :trader AND p.stock = :stock")
    Optional<Portfolio> findByTraderAndStockWithLock(@Param("trader") Trader trader, @Param("stock") Stock stock);

    List<Portfolio> findByTrader(Trader trader);

    @Modifying
    @Query("UPDATE Portfolio p SET p.quantity = :quantity WHERE p.id = :id AND p.version = :version")
    int updateQuantityWithVersion(@Param("id") Long id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    @Query("SELECT SUM(p.quantity) FROM Portfolio p WHERE p.stock = :stock")
    Long getTotalHoldingsForStock(@Param("stock") Stock stock);
}