package com.ead.payments.orders.complete;

import java.util.UUID;

public class CancelledOrderCompletionException extends RuntimeException {

    public CancelledOrderCompletionException(UUID orderId) {
        super("The cancelled order with id " + orderId + " cannot be completed");
    }
}
