package com.kshitij.trading.controller;

import com.kshitij.trading.dto.DirectAddRequest;
import com.kshitij.trading.dto.PortfolioResponse;
import com.kshitij.trading.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * Endpoint 4: Get Portfolio
     * GET /portfolio/{traderId}
     */
    @GetMapping("/{traderId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable String traderId) {
        log.info("GET /portfolio/{} - Retrieving portfolio", traderId);
        PortfolioResponse response = portfolioService.getPortfolio(traderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 6: Add to Portfolio (Direct)
     * POST /portfolio/add?traderId={traderId}
     */
    @PostMapping("/add")
    public ResponseEntity<PortfolioResponse.DirectAddResponse> directAddToPortfolio(
            @RequestParam String traderId,
            @Valid @RequestBody DirectAddRequest request) {
        log.info("POST /portfolio/add?traderId={} - Direct add: {} of {}",
                traderId, request.getQuantity(), request.getStock());
        PortfolioResponse.DirectAddResponse response = portfolioService.directAddToPortfolio(
                traderId, request.getStock(), request.getSector(), request.getQuantity());
        return ResponseEntity.ok(response);
    }
}