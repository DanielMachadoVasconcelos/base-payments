package com.ead.payments.orders;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

public record Order(
        UUID id,
        long version,
        OrderStatus status,
        Currency currency,
        Long amount,
        List<LineItem> lineItems  // NEW
) {

    public enum OrderStatus {
        PLACED,
        CANCELLED,
        COMPLETED
    }
}
