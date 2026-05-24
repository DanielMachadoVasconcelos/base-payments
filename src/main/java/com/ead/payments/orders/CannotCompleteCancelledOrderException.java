package com.ead.payments.orders;

import java.util.UUID;

public class CannotCompleteCancelledOrderException extends RuntimeException {

    public CannotCompleteCancelledOrderException(UUID orderId) {
        super("The cancelled order with id " + orderId + " cannot be completed");
    }
}
