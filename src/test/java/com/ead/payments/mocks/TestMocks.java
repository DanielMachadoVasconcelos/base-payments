package com.ead.payments.mocks;

import static com.ead.payments.mocks.WireMockProvider.IssuerServiceMockProvider;

public final class TestMocks {

    public static IssuerServiceMock setup(IssuerServiceMockProvider mockProvider){
        return new IssuerServiceWireMock(mockProvider);
    }
}
