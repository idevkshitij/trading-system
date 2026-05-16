package com.kshitij.trading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DirectAddRequest {

    @NotBlank(message = "stock symbol is required")
    private String stock;

    @NotBlank(message = "sector is required")
    private String sector;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
}