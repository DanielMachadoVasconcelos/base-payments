package com.ead.payments.orders.search;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchOrderResponse {

    private @NotNull UUID id;
    private @NotNull @Min(0L) Long version;
    private @NotNull Currency currency;
    private @NotNull Long amount;
}
