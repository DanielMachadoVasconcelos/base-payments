package com.ead.payments.orders.place;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuerAuthorizationResponse {
    private String authorizationKrn;
    private String status;
    private String statusReason;
}
