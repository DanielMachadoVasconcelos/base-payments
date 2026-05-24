package com.ead.payments.orders.cancel;

import java.util.UUID;

public class CompletedOrderCancellationException extends RuntimeException {

    public CompletedOrderCancellationException(UUID orderId) {
        super("The completed order with id " + orderId + " cannot be cancelled");
    }
}
