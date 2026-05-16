package com.kshitij.trading.controller;

import com.kshitij.trading.dto.OrderRequest;
import com.kshitij.trading.dto.OrderResponse;
import com.kshitij.trading.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Endpoint 1: Place an Order
     * POST /orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        log.info("POST /orders - Placing order for trader: {}", request.getTraderId());
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint 2: Fill an Order
     * POST /orders/{orderId}/fill
     */
    @PostMapping("/{orderId}/fill")
    public ResponseEntity<OrderResponse> fillOrder(@PathVariable Long orderId) {
        log.info("POST /orders/{}/fill - Filling order", orderId);
        OrderResponse response = orderService.fillOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 3: Cancel an Order
     * POST /orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        log.info("POST /orders/{}/cancel - Cancelling order", orderId);
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
}