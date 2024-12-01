package com.ead.payments.orders;

import java.util.UUID;

public record PlaceOrderCommand(
        UUID id,
        String currency,
        Long amount
) {
}