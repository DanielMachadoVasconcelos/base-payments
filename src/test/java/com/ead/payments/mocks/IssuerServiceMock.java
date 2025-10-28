package com.ead.payments.mocks;

import com.ead.payments.logging.CorrelationId;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import jakarta.validation.constraints.NotNull;

public interface IssuerServiceMock {

    StubMapping toAcceptTheAuthorizationWith(@NotNull CorrelationId expectedCorrelationId);
    StubMapping toRejectTheAuthorizationWith(@NotNull CorrelationId expectedCorrelationId);
}
