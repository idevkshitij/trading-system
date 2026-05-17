package com.kshitij.trading.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order placement response")
public class OrderResponse {

    @Schema(description = "Unique order ID", example = "1")
    private Long orderId;

    @Schema(description = "Current order status",
            example = "PENDING",
            allowableValues = {"PENDING", "FILLED", "CANCELLED", "REJECTED"})
    private String status;

    @Schema(description = "Additional information about the operation",
            example = "Order placed successfully")
    private String message;
}