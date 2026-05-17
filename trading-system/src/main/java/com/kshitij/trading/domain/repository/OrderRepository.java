package com.kshitij.trading.domain.repository;

import com.kshitij.trading.domain.entity.Order;
import com.kshitij.trading.domain.entity.Trader;
import com.kshitij.trading.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.trader = :trader AND o.status = :status")
    long countByTraderAndStatus(@Param("trader") Trader trader, @Param("status") OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.trader = :trader AND o.status = :status")
    List<Order> findPendingOrdersWithLock(@Param("trader") Trader trader, @Param("status") OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findById(Long id);

    List<Order> findByTraderAndStatus(Trader trader, OrderStatus status);

    List<Order> findByTrader(Trader trader);
}