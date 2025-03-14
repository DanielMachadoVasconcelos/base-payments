package com.ead.payments.orders.place;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class IssuerService {

    private final IssuerGateway issuerGateway;

    public Authorization authorize(PlaceOrderCommand command) {

        IssuerAuthorizationRequest request = new IssuerAuthorizationRequest(
            command.currency(),
            command.amount()
        );

        return Authorization.from(issuerGateway.authorize(request));
    }

    public sealed interface Authorization permits ApprovedAuthorization, RejectedAuthorization {

        static Authorization from(IssuerAuthorizationResponse response) {
            return switch (response.getStatus().toUpperCase()) {
                case "AUTHORIZED" -> new ApprovedAuthorization(response.getAuthorizationKrn());
                case "REJECTED" -> new RejectedAuthorization(response.getStatus(), response.getStatusReason());
                default -> throw new IllegalArgumentException(STR."Unknown status: \{response.getStatus()}");
            };
        }
    }

    record ApprovedAuthorization(String authorizationKrn) implements Authorization {}
    record RejectedAuthorization(String status, String statusReason) implements Authorization {}
}
