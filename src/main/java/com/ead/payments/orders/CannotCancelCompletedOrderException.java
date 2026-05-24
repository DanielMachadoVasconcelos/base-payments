package com.ead.payments.orders;

import java.util.UUID;

public class CannotCancelCompletedOrderException extends RuntimeException {

    public CannotCancelCompletedOrderException(UUID orderId) {
        super("The completed order with id " + orderId + " cannot be cancelled");
    }
}
