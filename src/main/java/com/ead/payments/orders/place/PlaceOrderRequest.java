package com.ead.payments.orders.place;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Currency;

public record PlaceOrderRequest(
        @NotNull Currency currency,
        @NotNull @Min(0L) Long amount
) {
}
