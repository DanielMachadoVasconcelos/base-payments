package com.ead.payments.orders.place;

public class UnknownAuthorizationStatus extends RuntimeException {
    public UnknownAuthorizationStatus(String message) {
        super(message);
    }
}
