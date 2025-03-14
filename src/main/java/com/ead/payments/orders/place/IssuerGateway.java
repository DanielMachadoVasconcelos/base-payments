package com.ead.payments.orders.place;

import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.AllArgsConstructor;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IssuerGateway {

    private IssuerClient issuerClient;

    @NewSpan("authorize-payment")
    @Observed(name = "issuer-gateway", contextualName = "Authorize Payment")
    @CircuitBreaker(maxAttempts = 2, openTimeout = 2000, label = "issuer-service-circuit-breaker")
    public IssuerAuthorizationResponse authorize(IssuerAuthorizationRequest request) {
           return issuerClient.authorize(request);
    }
}
