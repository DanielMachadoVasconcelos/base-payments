package com.ead.payments.orders.place;

import java.util.Currency;
import java.util.UUID;

public record PlaceOrderCommand(
        UUID id,
        Currency currency,
        Long amount
) {
}