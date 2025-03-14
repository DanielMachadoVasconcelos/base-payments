package com.ead.payments.orders.place;

public class IssuerDeclinedException extends RuntimeException {

    public IssuerDeclinedException(String message) {
        super(message);
    }
}
