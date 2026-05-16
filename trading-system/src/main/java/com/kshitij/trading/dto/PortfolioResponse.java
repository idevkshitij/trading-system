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
    private Map<String, Integer> positions;
    private Map<String, Integer> sectorBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirectAddResponse {
        private String traderId;
        private String stock;
        private Integer oldQuantity;
        private Integer newQuantity;
        private String message;
    }
}