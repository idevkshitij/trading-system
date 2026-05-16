package com.kshitij.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private String traderId;
    private Map<String, Integer> positions;      // stock -> quantity
    private Map<String, Integer> sectorBreakdown; // sector -> total quantity
}