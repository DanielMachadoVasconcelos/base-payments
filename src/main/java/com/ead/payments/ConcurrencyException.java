package com.ead.payments;

public class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String errorMessage) {
        super(errorMessage);
    }
}
