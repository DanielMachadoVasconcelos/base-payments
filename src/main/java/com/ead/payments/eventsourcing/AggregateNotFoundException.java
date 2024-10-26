package com.ead.payments.eventsourcing;

public class AggregateNotFoundException extends RuntimeException{
    public AggregateNotFoundException(String message) {
        super(message);
    }
}
