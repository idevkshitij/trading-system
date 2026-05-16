package com.kshitij.trading.exception;

public class InsufficientHoldingsException extends RuntimeException {
    public InsufficientHoldingsException(String stockSymbol, Integer current, Integer requested) {
        super("Insufficient holdings for " + stockSymbol + ". Current: " + current + ", Requested: " + requested);
    }
}