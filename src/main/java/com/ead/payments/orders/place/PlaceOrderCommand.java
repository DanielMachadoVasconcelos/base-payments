package com.ead.payments.orders.place;

import java.util.UUID;

public record PlaceOrderCommand(
        UUID id,
        String currency,
        Long amount
) {
}