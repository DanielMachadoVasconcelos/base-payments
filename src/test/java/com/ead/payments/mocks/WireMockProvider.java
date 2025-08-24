package com.ead.payments.mocks;

import com.github.tomakehurst.wiremock.WireMockServer;

public record WireMockProvider(MockProvider provider) {

    public static MockProvider of(WireMockServer mockIssuerService) {
        return new IssuerServiceMockProvider(mockIssuerService);
    }

    public record IssuerServiceMockProvider(WireMockServer wireMockServer) implements MockProvider {
    }
}
