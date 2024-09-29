package com.ead.payments;

public class AggregateNotFoundException extends RuntimeException{
    public AggregateNotFoundException(String message) {
        super(message);
    }
}
