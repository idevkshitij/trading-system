package com.kshitij.trading.service;

import com.kshitij.trading.domain.entity.*;
import com.kshitij.trading.domain.enums.OrderStatus;
import com.kshitij.trading.domain.enums.Side;
import com.kshitij.trading.domain.repository.OrderRepository;
import com.kshitij.trading.domain.repository.PortfolioRepository;
import com.kshitij.trading.domain.repository.StockRepository;
import com.kshitij.trading.domain.repository.TraderRepository;
import com.kshitij.trading.dto.OrderRequest;
import com.kshitij.trading.dto.OrderResponse;
import com.kshitij.trading.exception.InsufficientHoldingsException;
import com.kshitij.trading.exception.PendingOrderLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final TraderRepository traderRepository;
    private final StockRepository stockRepository;
    private final OrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioService portfolioService;

    private static final int MAX_PENDING_ORDERS = 3;

    /**
     * Place a new order
     * Uses REPEATABLE READ isolation to prevent phantom reads
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Placing order: trader={}, stock={}, quantity={}, side={}",
                request.getTraderId(), request.getStock(), request.getQuantity(), request.getSide());

        // Get or create trader
        Trader trader = traderRepository.findByTraderId(request.getTraderId())
                .orElseGet(() -> createNewTrader(request.getTraderId()));

        // Get stock (must exist from schema.sql)
        Stock stock = stockRepository.findBySymbol(request.getStock())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + request.getStock()));

        // Business Rule 1: Check pending order limit
        long pendingCount = orderRepository.countByTraderAndStatus(trader, OrderStatus.PENDING);
        if (pendingCount >= MAX_PENDING_ORDERS) {
            log.warn("Pending order limit exceeded for trader {}: {}/{}",
                    request.getTraderId(), pendingCount, MAX_PENDING_ORDERS);
            throw new PendingOrderLimitException(request.getTraderId(), (int) pendingCount);
        }

        // Business Rule 2: For SELL orders, check sufficient holdings
        if (Side.SELL.name().equalsIgnoreCase(request.getSide())) {
            int currentHoldings = portfolioService.getHoldingsForStock(trader, stock);
            if (currentHoldings < request.getQuantity()) {
                log.warn("Insufficient holdings for trader {}: stock={}, held={}, requested={}",
                        request.getTraderId(), request.getStock(), currentHoldings, request.getQuantity());
                throw new InsufficientHoldingsException(request.getStock(), currentHoldings, request.getQuantity());
            }
        }

        // Create and save order
        Order order = Order.builder()
                .trader(trader)
                .stock(stock)
                .quantity(request.getQuantity())
                .side(Side.valueOf(request.getSide().toUpperCase()))
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully: orderId={}", savedOrder.getId());

        return new OrderResponse(savedOrder.getId(), "PENDING", "Order placed successfully");
    }

    /**
     * Fill an existing PENDING order
     */
    @Transactional
    public OrderResponse fillOrder(Long orderId) {
        log.info("Filling order: orderId={}", orderId);

        // Use pessimistic lock to prevent concurrent fills
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate state
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot fill order {} - status is {}", orderId, order.getStatus());
            throw new BusinessException("Only PENDING orders can be filled. Current status: " + order.getStatus());
        }

        // Update portfolio based on side
        if (order.getSide() == Side.BUY) {
            portfolioService.addHoldings(order.getTrader(), order.getStock(), order.getQuantity());
            log.info("BUY order filled: added {} of {} to trader {}",
                    order.getQuantity(), order.getStock().getSymbol(), order.getTrader().getTraderId());
        } else {
            portfolioService.removeHoldings(order.getTrader(), order.getStock(), order.getQuantity());
            log.info("SELL order filled: removed {} of {} from trader {}",
                    order.getQuantity(), order.getStock().getSymbol(), order.getTrader().getTraderId());
        }

        // Update order status
        order.setStatus(OrderStatus.FILLED);
        orderRepository.save(order);

        String message = String.format("Order filled. New holdings for %s: %d",
                order.getStock().getSymbol(),
                portfolioService.getHoldingsForStock(order.getTrader(), order.getStock()));

        return new OrderResponse(order.getId(), "FILLED", message);
    }

    /**
     * Cancel a PENDING order
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Cancelling order: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate state
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot cancel order {} - status is {}", orderId, order.getStatus());
            throw new BusinessException("Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }

        // Update order status (no portfolio change)
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order cancelled: orderId={}", orderId);
        return new OrderResponse(order.getId(), "CANCELLED", "Order cancelled successfully");
    }

    private Trader createNewTrader(String traderId) {
        Trader trader = Trader.builder()
                .traderId(traderId)
                .build();
        return traderRepository.save(trader);
    }

    // Custom exception for business rules
    private static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}