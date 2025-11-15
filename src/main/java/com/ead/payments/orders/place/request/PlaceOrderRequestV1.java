package com.ead.payments.orders.place.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Currency;

public record PlaceOrderRequestV1(
        @NotNull Currency currency,
        @NotNull @Min(0L) Long amount
) {
    // This is the current PlaceOrderRequest renamed
}
