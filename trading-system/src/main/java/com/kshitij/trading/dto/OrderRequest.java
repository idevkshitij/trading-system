package com.kshitij.trading.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Order placement request")
public class OrderRequest {

    @NotBlank(message = "traderId is required")
    @Schema(description = "Unique identifier for the trader",
            example = "T001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String traderId;

    @NotBlank(message = "stock symbol is required")
    @Schema(description = "Stock symbol",
            example = "AAPL",
            allowableValues = {"AAPL", "MSFT", "GOOGL", "TSLA", "NVDA", "JPM", "GS", "BAC", "MS", "WFC", "XOM", "JNJ"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String stock;

    @NotBlank(message = "sector is required")
    @Schema(description = "Sector of the stock",
            example = "TECH",
            allowableValues = {"TECH", "FINANCE", "ENERGY", "HEALTHCARE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sector;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    @Schema(description = "Number of shares to trade",
            example = "50",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @NotBlank(message = "side is required")
    @Schema(description = "Trade direction",
            example = "BUY",
            allowableValues = {"BUY", "SELL"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String side;
}