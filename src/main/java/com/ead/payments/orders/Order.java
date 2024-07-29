package com.ead.payments.orders;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.UUID;

public record Order(
        UUID id,
        Long version,
        String currency,
        Long amount
) {

        public Order(UUID id, Long version, String currency, Long amount) {
            this.id = Preconditions.checkNotNull(Optional.ofNullable(id).orElseGet(UUID::randomUUID), "The id is required");
            this.version = Preconditions.checkNotNull(version, "The version is required");
            this.currency = Preconditions.checkNotNull(currency, "The currency is required");
            this.amount = Preconditions.checkNotNull(amount, "The amount is required");
        }
}
