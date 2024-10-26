package com.ead.payments.eventsourcing;

public class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String errorMessage) {
        super(errorMessage);
    }
}
