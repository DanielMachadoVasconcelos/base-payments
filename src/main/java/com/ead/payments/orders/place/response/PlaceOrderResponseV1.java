package com.ead.payments.orders.place.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponseV1 {

    private UUID id;
    private @NotBlank Currency currency;
    private @NotNull Long amount;

}
