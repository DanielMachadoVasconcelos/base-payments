package com.ead.payments.orders.place;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssuerAuthorizationRequest {

    private String currency;
    private Long amount;
}
