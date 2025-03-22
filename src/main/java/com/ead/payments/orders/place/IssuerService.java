package com.ead.payments.orders.place;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
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
                case "AUTHORIZED" -> new ApprovedAuthorization(new AuthorizationKrn(response.getAuthorizationKrn()));
                case "REJECTED" -> new RejectedAuthorization(response.getStatus(), response.getStatusReason());
                default -> throw new UnknownAuthorizationStatus(STR."Unknown authorization status: \{response.getStatus()}. The result of the authorization request is unknown and can't be parsed.");
            };
        }
    }

    record ApprovedAuthorization(AuthorizationKrn authorizationKrn) implements Authorization {}
    record RejectedAuthorization(String status, String statusReason) implements Authorization {}

    record AuthorizationKrn(String value) {

        private static final Pattern AUTH_KRN_PATTERN = Pattern.compile(
            "^krn:payments:authorization:[a-z]{2,3}-[a-z]+-\\d+:\\d{14}:transaction:[0-9a-fA-F\\-]{36}(?:\\?version=\\d+\\.\\d+\\.\\d+)?$"
        );

        public AuthorizationKrn {
            Preconditions.checkNotNull(value, "AuthorizationKrn value can't be null");
            Preconditions.checkArgument(!value.isBlank(), "AuthorizationKrn value can't be blank");
            Preconditions.checkArgument(
                    AUTH_KRN_PATTERN.matcher(value).matches(),
                    STR."AuthorizationKrn value is not in the correct format: \{value}"
            );
        }
    }
}

