package com.ead.payments.orders;

import java.util.UUID;

public class OrderStateTransitionException extends RuntimeException {

    public OrderStateTransitionException(
            UUID orderId,
            Order.OrderStatus currentStatus,
            Order.OrderStatus targetStatus
    ) {
        super("The order with id " + orderId + " cannot transition from " + currentStatus + " to " + targetStatus);
    }
}
