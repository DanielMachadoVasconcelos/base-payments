package com.ead.payments.orders.place;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssuerAuthorizationRequest {

    private Currency currency;
    private Long amount;
}
