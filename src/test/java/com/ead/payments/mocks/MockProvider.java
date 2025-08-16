package com.ead.payments.mocks;

public interface MockProvider {
    <T> T wireMockServer();
}
