package com.ead.payments.orders.events;

import java.util.UUID;

public class OrderEventNotFoundException extends RuntimeException {

    public OrderEventNotFoundException(UUID orderId, UUID eventId) {
        super("Event %s was not found for order %s".formatted(eventId, orderId));
    }
}
