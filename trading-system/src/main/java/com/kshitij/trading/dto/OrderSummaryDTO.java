package com.kshitij.trading.dto;

import com.kshitij.trading.domain.enums.OrderStatus;
import com.kshitij.trading.domain.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {
    private Long id;
    private String stockSymbol;
    private Integer quantity;
    private Side side;
    private OrderStatus status;
    private LocalDateTime createdAt;
}