package com.ead.payments.orders.search;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchOrderResponse {

    private @NotNull UUID id;
    private @NotNull @Min(0L) Long version;
    private @NotBlank String currency;
    private @NotNull Long amount;
}
