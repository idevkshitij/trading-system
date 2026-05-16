package com.kshitij.trading.service;

import com.kshitij.trading.dto.OverlapResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SectorOverlapServiceTest {

    private SectorOverlapService sectorOverlapService;

    @BeforeEach
    void setUp() {
        sectorOverlapService = new SectorOverlapService();
    }

    @Test
    @DisplayName("HIGH risk when overlap >= 60%")
    void testHighRisk() {
        Set<String> portfolio = Set.of("AAPL", "TSLA", "NVDA");
        OverlapResponse response = sectorOverlapService.calculateOverlap(portfolio);

        assertThat(response.getRiskFlag()).isEqualTo("HIGH");
        assertThat(response.getDominantBasket()).isEqualTo("TECH_HEAVY");
    }

    @Test
    @DisplayName("MEDIUM risk when overlap >= 40% and < 60%")
    void testMediumRisk() {
        Set<String> portfolio = Set.of("AAPL", "JPM");
        OverlapResponse response = sectorOverlapService.calculateOverlap(portfolio);

        assertThat(response.getRiskFlag()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("LOW risk when all overlaps < 40%")
    void testLowRisk() {
        Set<String> portfolio = Set.of("XOM", "WFC");
        OverlapResponse response = sectorOverlapService.calculateOverlap(portfolio);

        assertThat(response.getRiskFlag()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("Empty portfolio returns 0% overlap and LOW risk")
    void testEmptyPortfolio() {
        Set<String> portfolio = Set.of();
        OverlapResponse response = sectorOverlapService.calculateOverlap(portfolio);

        assertThat(response.getRiskFlag()).isEqualTo("LOW");
        for (OverlapResponse.OverlapDetail detail : response.getOverlaps()) {
            assertThat(detail.getOverlap()).isEqualTo("0.00%");
        }
    }

    @Test
    @DisplayName("Calculate overlap percentage correctly - 75% overlap")
    void testOverlapCalculationSeventyFivePercent() {
        Set<String> portfolio = Set.of("AAPL", "TSLA", "NVDA");
        OverlapResponse response = sectorOverlapService.calculateOverlap(portfolio);

        OverlapResponse.OverlapDetail techHeavy = response.getOverlaps().stream()
                .filter(d -> d.getBasket().equals("TECH_HEAVY"))
                .findFirst()
                .orElseThrow();

        assertThat(techHeavy.getOverlap()).isEqualTo("75.00%");
    }

    @Test
    @DisplayName("Calculate overlap percentage correctly - 50% overlap")
    void testOverlapCalculationFiftyPercent() {
        // Portfolio: AAPL, TSLA (2 stocks)
        // BALANCED: AAPL, JPM, XOM, JNJ, TSLA (5 stocks)
        // Common: AAPL, TSLA (2 stocks)
        // Formula: 2 * 2 / (2 + 5) * 100 = 4/7*100 = 57.14%
        Set<String> portfolio = Set.of("AAPL", "TSLA");
        OverlapResponse response = sectorOverlapService.calculateOverlap(portfolio);

        OverlapResponse.OverlapDetail balanced = response.getOverlaps().stream()
                .filter(d -> d.getBasket().equals("BALANCED"))
                .findFirst()
                .orElseThrow();

        assertThat(balanced.getOverlap()).isEqualTo("57.14%");
    }

    @Test
    @DisplayName("Zero overlap with FINANCE_HEAVY when no finance stocks")
    void testZeroOverlapWithFinance() {
        Set<String> portfolio = Set.of("AAPL", "MSFT", "GOOGL");
        OverlapResponse response = sectorOverlapService.calculateOverlap(portfolio);

        OverlapResponse.OverlapDetail financeHeavy = response.getOverlaps().stream()
                .filter(d -> d.getBasket().equals("FINANCE_HEAVY"))
                .findFirst()
                .orElseThrow();

        assertThat(financeHeavy.getOverlap()).isEqualTo("0.00%");
    }
}