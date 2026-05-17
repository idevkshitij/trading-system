package com.kshitij.trading.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trade direction - BUY increases holdings, SELL decreases holdings")
public enum Side {
    BUY,
    SELL
}