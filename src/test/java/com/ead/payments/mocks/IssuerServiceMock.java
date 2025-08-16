package com.ead.payments.mocks;

import com.ead.payments.logging.CorrelationId;
import jakarta.validation.constraints.NotNull;

public interface IssuerServiceMock {

    boolean toAcceptTheAuthorizationWith(@NotNull CorrelationId expectedCorrelationId);
    boolean toRejectTheAuthorizationWith(@NotNull CorrelationId expectedCorrelationId);
}
