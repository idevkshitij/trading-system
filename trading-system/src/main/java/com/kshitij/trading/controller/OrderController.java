package com.kshitij.trading.controller;

import com.kshitij.trading.domain.entity.Order;
import com.kshitij.trading.domain.entity.Trader;
import com.kshitij.trading.domain.repository.OrderRepository;
import com.kshitij.trading.domain.repository.TraderRepository;
import com.kshitij.trading.dto.OrderRequest;
import com.kshitij.trading.dto.OrderResponse;
import com.kshitij.trading.dto.ErrorResponse;
import com.kshitij.trading.dto.OrderSummaryDTO;
import com.kshitij.trading.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "Endpoints for placing, filling, and cancelling orders")
public class OrderController {

    private final OrderService orderService;
    private final TraderRepository traderRepository;
    private final OrderRepository orderRepository;

    @PostMapping
    @Operation(
            summary = "Place a new order",
            description = "Creates a PENDING order for a trader. Validates business rules: max 3 pending orders per trader and sufficient holdings for SELL orders."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order placed successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = "{\"orderId\":1,\"status\":\"PENDING\",\"message\":\"Order placed successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request (validation error)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Business rule violation (3 pending orders or insufficient holdings)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> placeOrder(
            @Parameter(description = "Order details", required = true)
            @Valid @RequestBody OrderRequest request) {

        log.info("POST /orders - Placing order for trader: {}", request.getTraderId());
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{orderId}/fill")
    @Operation(
            summary = "Fill an order",
            description = "Executes a PENDING order. BUY orders increase holdings, SELL orders decrease holdings."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order filled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order is not in PENDING state")
    })
    public ResponseEntity<OrderResponse> fillOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long orderId) {

        log.info("POST /orders/{}/fill - Filling order", orderId);
        OrderResponse response = orderService.fillOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
            summary = "Cancel an order",
            description = "Cancels a PENDING order. Does not affect portfolio holdings."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order is not in PENDING state")
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long orderId) {

        log.info("POST /orders/{}/cancel - Cancelling order", orderId);
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trader/{traderId}")
    @Operation(
            summary = "Get all orders for a trader",
            description = "Returns all orders (PENDING, FILLED, CANCELLED) for a specific trader"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trader not found")
    })
    public ResponseEntity<List<OrderSummaryDTO>> getOrdersByTrader(@PathVariable String traderId) {
        Trader trader = traderRepository.findByTraderId(traderId)
                .orElseThrow(() -> new RuntimeException("Trader not found: " + traderId));
        List<Order> orders = orderRepository.findByTrader(trader);

        List<OrderSummaryDTO> orderSummaries = orders.stream()
                .map(order -> OrderSummaryDTO.builder()
                        .id(order.getId())
                        .stockSymbol(order.getStock().getSymbol())
                        .quantity(order.getQuantity())
                        .side(order.getSide())
                        .status(order.getStatus())
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderSummaries);
    }
}