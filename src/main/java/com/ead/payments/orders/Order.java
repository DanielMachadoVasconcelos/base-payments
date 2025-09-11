package com.ead.payments.orders;

import java.util.Currency;
import java.util.UUID;

public record Order(
        UUID id,
        long version,
        OrderStatus status,
        Currency currency,
        Long amount
) {

    public enum OrderStatus {
        PLACED,
        CANCELLED,
        COMPLETED
    }
}
