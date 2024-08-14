package com.ead.payments.orders;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

public record UpdateOrderRequest(
        @Nullable @Min(1) Long version,
        @Nullable @Min(1) Long amount
) {
}
