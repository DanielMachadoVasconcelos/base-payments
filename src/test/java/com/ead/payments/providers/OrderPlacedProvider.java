package com.ead.payments.providers;

import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.ead.payments.mocks.WireMockProvider;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.ead.payments.orders.place.response.PlaceOrderResponseV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Currency;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import static com.ead.payments.mocks.WireMockProvider.IssuerServiceMockProvider;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class OrderPlacedProvider {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final WireMockServer mockIssuerService;

    public OrderPlacedProvider(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            @Qualifier("issuer-service") WireMockServer mockIssuerService
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.mockIssuerService = mockIssuerService;
    }

    public PlaceOrderResponseV1 placeOrder() throws Exception {
        CorrelationId expectedCorrelationId = CorrelationId.random();
        TestMocks.setup((IssuerServiceMockProvider) WireMockProvider.of(mockIssuerService))
                .toAcceptTheAuthorizationWith(expectedCorrelationId);

        var request = new PlaceOrderRequestV1(Currency.getInstance("USD"), 100L);

        var orderPlacedResponse = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedCorrelationId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return objectMapper.readValue(
                orderPlacedResponse.andReturn().getResponse().getContentAsString(),
                PlaceOrderResponseV1.class
        );
    }
}
