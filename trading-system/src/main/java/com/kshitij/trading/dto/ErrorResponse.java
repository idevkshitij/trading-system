package com.kshitij.trading.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response when something goes wrong")
public class ErrorResponse {

    @Schema(description = "Error code",
            example = "PENDING_ORDER_LIMIT",
            allowableValues = {"PENDING_ORDER_LIMIT", "INSUFFICIENT_HOLDINGS", "ORDER_NOT_FOUND", "BUSINESS_RULE_VIOLATION", "VALIDATION_ERROR"})
    private String error;

    @Schema(description = "Human readable error message",
            example = "Trader T001 already has 3 pending orders. Cancel an order before placing new one.")
    private String message;

    @Schema(description = "Timestamp when error occurred",
            example = "2026-05-17T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "API path that caused the error",
            example = "/orders")
    private String path;
}