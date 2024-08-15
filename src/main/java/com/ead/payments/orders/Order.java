package com.ead.payments.orders;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record Order(
        UUID id,
        Long version,
        String currency,
        Long amount,
        Set<LineItem> lineItems
) {

    public Order {
        Preconditions.checkNotNull(Optional.ofNullable(id).orElseGet(UUID::randomUUID), "The id is required");
        Preconditions.checkNotNull(Optional.ofNullable(lineItems).orElse(Set.of()), "The line items are required");

        Preconditions.checkNotNull(version, "The version is required");
        Preconditions.checkArgument(version > 0, "The version must be greater than 0");

        Preconditions.checkNotNull(currency, "The currency is required");
        Preconditions.checkArgument(!currency.isBlank(), "The currency is required");
        Preconditions.checkArgument(currency.length() == 3, "The currency must be in ISO 4217 format");

        Preconditions.checkNotNull(amount, "The amount is required");
        Preconditions.checkArgument(amount > 0, "The amount must be greater than 0");

    }
}
