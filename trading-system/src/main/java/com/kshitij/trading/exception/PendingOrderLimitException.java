package com.kshitij.trading.exception;

public class PendingOrderLimitException extends RuntimeException {
    public PendingOrderLimitException(String traderId, int pendingCount) {
        super("Trader " + traderId + " already has " + pendingCount + " pending orders. Cancel an order before placing new one.");
    }
}