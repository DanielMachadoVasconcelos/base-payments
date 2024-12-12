package com.ead.payments.orders.place;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotBlank String currency,
        @NotNull Long amount
) {
}
