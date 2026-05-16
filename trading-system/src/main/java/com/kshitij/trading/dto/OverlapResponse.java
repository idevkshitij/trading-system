package com.kshitij.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverlapResponse {
    private List<OverlapDetail> overlaps;
    private String dominantBasket;
    private String riskFlag;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverlapDetail {
        private String basket;
        private String overlap; // formatted as percentage string like "60.00%"
    }
}