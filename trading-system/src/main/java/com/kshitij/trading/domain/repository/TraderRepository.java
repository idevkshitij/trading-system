package com.kshitij.trading.domain.repository;

import com.kshitij.trading.domain.entity.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraderRepository extends JpaRepository<Trader, Long> {

    Optional<Trader> findByTraderId(String traderId);

    boolean existsByTraderId(String traderId);
}