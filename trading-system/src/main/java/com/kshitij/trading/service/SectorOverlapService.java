package com.kshitij.trading.service;

import com.kshitij.trading.dto.OverlapResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SectorOverlapService {

    // Benchmark baskets (hardcoded as per requirement)
    private static final Map<String, Set<String>> BENCHMARKS = Map.of(
            "TECH_HEAVY", Set.of("AAPL", "MSFT", "GOOGL", "TSLA", "NVDA"),
            "FINANCE_HEAVY", Set.of("JPM", "GS", "BAC", "MS", "WFC"),
            "BALANCED", Set.of("AAPL", "JPM", "XOM", "JNJ", "TSLA")
    );

    /**
     * Calculate overlap between trader's portfolio and benchmark baskets
     * Pure Java logic - NO database calls inside this method
     *
     * @param portfolioStocks List of stock symbols the trader holds
     * @return OverlapResponse with percentages, dominant basket, and risk flag
     */
    public OverlapResponse calculateOverlap(Set<String> portfolioStocks) {
        if (portfolioStocks == null || portfolioStocks.isEmpty()) {
            return getEmptyOverlapResponse();
        }

        List<OverlapResponse.OverlapDetail> overlaps = new ArrayList<>();
        double highestOverlap = 0.0;
        String dominantBasket = null;

        // Calculate overlap for each benchmark
        for (Map.Entry<String, Set<String>> entry : BENCHMARKS.entrySet()) {
            String basketName = entry.getKey();
            Set<String> basketStocks = entry.getValue();

            double overlap = calculateSingleOverlap(portfolioStocks, basketStocks);
            overlaps.add(new OverlapResponse.OverlapDetail(basketName, String.format("%.2f%%", overlap)));

            if (overlap > highestOverlap) {
                highestOverlap = overlap;
                dominantBasket = basketName;
            }
        }

        // Determine risk flag based on highest overlap
        String riskFlag = determineRiskFlag(highestOverlap);

        return new OverlapResponse(overlaps, dominantBasket, riskFlag);
    }

    /**
     * Calculate overlap percentage for a single basket
     * Formula: [2 x |common stocks| / (|portfolio| + |basket|)] x 100
     */
    private double calculateSingleOverlap(Set<String> portfolio, Set<String> basket) {
        if (portfolio.isEmpty() || basket.isEmpty()) {
            return 0.0;
        }

        // Find common stocks
        Set<String> common = new HashSet<>(portfolio);
        common.retainAll(basket);

        int commonCount = common.size();
        int portfolioSize = portfolio.size();
        int basketSize = basket.size();

        // Formula: 2 x common / (portfolio + basket) x 100
        double result = (2.0 * commonCount) / (portfolioSize + basketSize) * 100;

        return Math.round(result * 100.0) / 100.0; // Round to 2 decimal places
    }

    private String determineRiskFlag(double overlap) {
        if (overlap >= 60.0) {
            return "HIGH";
        } else if (overlap >= 40.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private OverlapResponse getEmptyOverlapResponse() {
        List<OverlapResponse.OverlapDetail> emptyOverlaps = BENCHMARKS.keySet().stream()
                .map(name -> new OverlapResponse.OverlapDetail(name, "0.00%"))
                .collect(Collectors.toList());
        return new OverlapResponse(emptyOverlaps, null, "LOW");
    }

    // For testing purposes - allows injecting different benchmarks
    public Map<String, Set<String>> getBenchmarks() {
        return Collections.unmodifiableMap(BENCHMARKS);
    }
}