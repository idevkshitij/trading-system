package com.kshitij.trading.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current state of the order in its lifecycle")
public enum OrderStatus {
    PENDING,    // Awaiting execution
    FILLED,     // Successfully executed
    CANCELLED,  // Cancelled before execution
    REJECTED    // Failed validation at placement
}