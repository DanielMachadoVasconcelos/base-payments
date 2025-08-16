package com.ead.payments.mocks;


import com.ead.payments.logging.CorrelationId;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.validation.constraints.NotNull;
import org.junit.platform.commons.util.Preconditions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public record IssuerServiceWireMock(WireMockServer mockIssuerService) implements IssuerServiceMock {

    public IssuerServiceWireMock(@NotNull MockProvider mockIssuerService) {
        this(Preconditions.notNull((WireMockServer) mockIssuerService.wireMockServer(), "WireMockServer can't be null"));
    }

    @Override
    public boolean toAcceptTheAuthorizationWith(@NotNull CorrelationId expectedCorrelationId) {
        // setup: the issuer service mock will return an authorized response
        var expectedResponse = aResponse()
                .withStatus(201)
                .withTransformers("response-template")
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                        "  \"authorizationKrn\": \"krn:payments:authorization:eu-west-1:{{now format='yyyyMMddHHmmss'}}:transaction:{{randomValue type='UUID'}}?version=1.0.0\",\n" +
                        "  \"status\": \"AUTHORIZED\",\n" +
                        "  \"statusReason\": null\n" +
                        "}"
                );

        // setup: the issuer service mock will return an authorized response
        mockIssuerService.stubFor(
                WireMock.post(urlEqualTo("/authorization"))
                        .withHeader("X-Correlation-ID", WireMock.equalTo(expectedCorrelationId.toString()))
                        .willReturn(expectedResponse)
        );

        return true;
    }

    @Override
    public boolean toRejectTheAuthorizationWith(@NotNull CorrelationId expectedCorrelationId) {
        // setup: the issuer service mock will return an authorized response
        var expectedResponse = aResponse()
                .withStatus(200)
                .withTransformers("response-template")
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                        "  \"status\": \"REJECTED\",\n" +
                        "  \"statusReason\": \"Insufficient funds\"\n" +
                        "}"
                );

        // setup: the issuer service mock will return an authorized response
        mockIssuerService.stubFor(
                WireMock.post(urlEqualTo("/authorization"))
                        .withHeader("X-Correlation-ID", WireMock.equalTo(expectedCorrelationId.toString()))
                        .willReturn(expectedResponse)
        );

        return true;
    }
}
