package com.kshitij.trading.controller;

import com.kshitij.trading.dto.OverlapResponse;
import com.kshitij.trading.service.SectorOverlapService;
import com.kshitij.trading.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/overlap")
@RequiredArgsConstructor
@Slf4j
public class OverlapController {

    private final SectorOverlapService sectorOverlapService;
    private final PortfolioService portfolioService;

    /**
     * Endpoint 5: Sector Overlap Analysis
     * GET /overlap/{traderId}
     */
    @GetMapping("/{traderId}")
    public ResponseEntity<OverlapResponse> getOverlap(@PathVariable String traderId) {
        log.info("GET /overlap/{} - Calculating sector overlap", traderId);
        OverlapResponse response = portfolioService.calculateOverlap(traderId);
        return ResponseEntity.ok(response);
    }
}