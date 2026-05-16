package com.kshitij.trading.service;

import com.kshitij.trading.domain.entity.Portfolio;
import com.kshitij.trading.domain.entity.Stock;
import com.kshitij.trading.domain.entity.Trader;
import com.kshitij.trading.domain.repository.PortfolioRepository;
import com.kshitij.trading.dto.PortfolioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    /**
     * Get holdings for a trader
     */
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(String traderId) {
        // This will be implemented in Module 4 with actual Trader entity
        // For now, placeholder
        return new PortfolioResponse(traderId, Map.of(), Map.of());
    }

    /**
     * Get current holdings for a specific stock (with lock for concurrent operations)
     */
    @Transactional
    public int getHoldingsForStock(Trader trader, Stock stock) {
        return portfolioRepository.findByTraderAndStockWithLock(trader, stock)
                .map(Portfolio::getQuantity)
                .orElse(0);
    }

    /**
     * Add holdings to portfolio (for BUY orders and direct add)
     */
    @Transactional
    public void addHoldings(Trader trader, Stock stock, int quantity) {
        log.debug("Adding {} of {} to trader {}", quantity, stock.getSymbol(), trader.getTraderId());

        Portfolio portfolio = portfolioRepository.findByTraderAndStockWithLock(trader, stock)
                .orElse(Portfolio.builder()
                        .trader(trader)
                        .stock(stock)
                        .quantity(0)
                        .build());

        portfolio.setQuantity(portfolio.getQuantity() + quantity);
        portfolioRepository.save(portfolio);
    }

    /**
     * Remove holdings from portfolio (for SELL orders)
     */
    @Transactional
    public void removeHoldings(Trader trader, Stock stock, int quantity) {
        log.debug("Removing {} of {} from trader {}", quantity, stock.getSymbol(), trader.getTraderId());

        Portfolio portfolio = portfolioRepository.findByTraderAndStockWithLock(trader, stock)
                .orElseThrow(() -> new RuntimeException("No holdings found for stock: " + stock.getSymbol()));

        if (portfolio.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient holdings");
        }

        portfolio.setQuantity(portfolio.getQuantity() - quantity);
        portfolioRepository.save(portfolio);
    }

    /**
     * Directly add to portfolio (bypasses order system)
     */
    @Transactional
    public void directAddToPortfolio(Trader trader, Stock stock, int quantity) {
        log.info("Direct add: {} of {} to trader {}", quantity, stock.getSymbol(), trader.getTraderId());
        addHoldings(trader, stock, quantity);
    }

    /**
     * Get all stocks in trader's portfolio (for overlap calculation)
     */
    @Transactional(readOnly = true)
    public List<Stock> getTraderStocks(Trader trader) {
        return portfolioRepository.findByTrader(trader).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(Portfolio::getStock)
                .toList();
    }
}