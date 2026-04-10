package com.ead.payments.orders.place;

import io.micrometer.observation.annotation.Observed;
import io.micrometer.observation.annotation.ObservationKeyValue;
import io.micrometer.observation.aop.Cardinality;
import lombok.AllArgsConstructor;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IssuerGateway {

    private IssuerClient issuerClient;

    @Observed(
            name = "issuer-gateway",
            contextualName = "authorize-payment",
            lowCardinalityKeyValues = {"component", "issuer-gateway", "operation", "authorize"}
    )
    @CircuitBreaker(maxAttempts = 2, openTimeout = 2000, label = "issuer-service-circuit-breaker")
    public IssuerAuthorizationResponse authorize(
            @ObservationKeyValue(key = "currency", expression = "currency.currencyCode", cardinality = Cardinality.LOW)
            IssuerAuthorizationRequest request) {
           return issuerClient.authorize(request);
    }
}
