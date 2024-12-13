package com.ead.payments.orders;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID orderId) {
        super("The order with id " + orderId + " was not found");
    }
}
