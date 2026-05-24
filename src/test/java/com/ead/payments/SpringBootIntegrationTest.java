package com.ead.payments;

import com.ead.payments.mocks.WireMockProvider;
import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.ead.payments.orders.place.response.PlaceOrderResponseV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Currency;
import java.util.UUID;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.ead.payments.mocks.WireMockProvider.IssuerServiceMockProvider;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@EnableWireMock({@ConfigureWireMock(name = "issuer-service", baseUrlProperties = "issuer.client.base-url")})
public class SpringBootIntegrationTest {

    @InjectWireMock("issuer-service")
    private WireMockServer mockIssuerService;

    protected IssuerServiceMockProvider issuerService() {
        return (IssuerServiceMockProvider) WireMockProvider.of(mockIssuerService);
    }

    protected UUID placeOrder(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        CorrelationId expectedCorrelationId = CorrelationId.random();
        TestMocks.setup(issuerService())
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
        ).getId();
    }

}
