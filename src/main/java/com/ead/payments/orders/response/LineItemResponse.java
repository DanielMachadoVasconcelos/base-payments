package com.ead.payments.orders.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LineItemResponse(
    @NotBlank String name,
    @Min(1) int quantity,
    @NotNull @Min(0L) Long unitPrice,
    String reference  // nullable
) {
}

