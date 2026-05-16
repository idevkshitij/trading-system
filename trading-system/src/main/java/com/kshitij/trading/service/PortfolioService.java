package com.kshitij.trading.service;

import com.kshitij.trading.domain.entity.Portfolio;
import com.kshitij.trading.domain.entity.Stock;
import com.kshitij.trading.domain.entity.Trader;
import com.kshitij.trading.domain.repository.PortfolioRepository;
import com.kshitij.trading.domain.repository.StockRepository;
import com.kshitij.trading.domain.repository.TraderRepository;
import com.kshitij.trading.dto.OverlapResponse;
import com.kshitij.trading.dto.PortfolioResponse;
import com.kshitij.trading.dto.PortfolioResponse.DirectAddResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TraderRepository traderRepository;
    private final StockRepository stockRepository;
    private final SectorOverlapService sectorOverlapService;

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(String traderId) {
        Trader trader = traderRepository.findByTraderId(traderId)
                .orElseThrow(() -> new RuntimeException("Trader not found: " + traderId));

        List<Portfolio> holdings = portfolioRepository.findByTrader(trader);

        Map<String, Integer> positions = new HashMap<>();
        Map<String, Integer> sectorBreakdown = new HashMap<>();

        for (Portfolio p : holdings) {
            if (p.getQuantity() > 0) {
                positions.put(p.getStock().getSymbol(), p.getQuantity());
                String sector = p.getStock().getSector();
                sectorBreakdown.put(sector, sectorBreakdown.getOrDefault(sector, 0) + p.getQuantity());
            }
        }

        return new PortfolioResponse(traderId, positions, sectorBreakdown);
    }

    @Transactional
    public int getHoldingsForStock(Trader trader, Stock stock) {
        return portfolioRepository.findByTraderAndStockWithLock(trader, stock)
                .map(Portfolio::getQuantity)
                .orElse(0);
    }

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

    @Transactional
    public DirectAddResponse directAddToPortfolio(String traderId, String stockSymbol, String sector, int quantity) {
        Trader trader = traderRepository.findByTraderId(traderId)
                .orElseGet(() -> createNewTrader(traderId));

        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseGet(() -> createNewStock(stockSymbol, sector));

        int oldQuantity = getHoldingsForStock(trader, stock);
        addHoldings(trader, stock, quantity);
        int newQuantity = oldQuantity + quantity;

        return new DirectAddResponse(
                traderId,
                stockSymbol,
                oldQuantity,
                newQuantity,
                "Successfully added " + quantity + " shares of " + stockSymbol
        );
    }

    @Transactional(readOnly = true)
    public OverlapResponse calculateOverlap(String traderId) {
        Trader trader = traderRepository.findByTraderId(traderId)
                .orElseThrow(() -> new RuntimeException("Trader not found: " + traderId));

        Set<String> portfolioStocks = portfolioRepository.findByTrader(trader).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> p.getStock().getSymbol())
                .collect(Collectors.toSet());

        return sectorOverlapService.calculateOverlap(portfolioStocks);
    }

    @Transactional(readOnly = true)
    public List<Stock> getTraderStocks(Trader trader) {
        return portfolioRepository.findByTrader(trader).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(Portfolio::getStock)
                .toList();
    }

    private Trader createNewTrader(String traderId) {
        Trader trader = Trader.builder()
                .traderId(traderId)
                .build();
        return traderRepository.save(trader);
    }

    private Stock createNewStock(String symbol, String sector) {
        Stock stock = Stock.builder()
                .symbol(symbol)
                .name(symbol)
                .sector(sector)
                .build();
        return stockRepository.save(stock);
    }
}