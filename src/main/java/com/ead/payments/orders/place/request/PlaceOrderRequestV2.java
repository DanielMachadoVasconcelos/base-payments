package com.ead.payments.orders.place.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Currency;
import java.util.List;

public record PlaceOrderRequestV2(
    @NotNull Currency currency,
    @NotNull @Size(min = 1, max = 100) @Valid List<LineItemRequest> lineItems,
    @Min(0L) Long amount  // Optional: for validation if provided
) {
}
