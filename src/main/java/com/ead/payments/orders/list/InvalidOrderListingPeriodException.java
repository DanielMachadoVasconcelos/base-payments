package com.ead.payments.orders.list;

import java.time.Instant;

public class InvalidOrderListingPeriodException extends IllegalArgumentException {

    public InvalidOrderListingPeriodException(Instant createdFrom, Instant createdTo) {
        super("Order listing start must not be after its end: %s > %s"
                .formatted(createdFrom, createdTo));
    }
}
