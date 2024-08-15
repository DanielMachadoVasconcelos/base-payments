package com.ead.payments.orders;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record PlaceOrderRequest(
        @Nullable UUID id,
        @Nullable @Min(1) Long version,
        @NotBlank String currency,
        @Min(1) @NotNull Long amount,
        @Nullable Set<LineItem> lineItems
) {
}
