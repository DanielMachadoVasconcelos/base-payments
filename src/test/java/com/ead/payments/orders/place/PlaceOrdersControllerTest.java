package com.ead.payments.orders.place;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.ead.payments.orders.place.request.LineItemRequest;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.ead.payments.orders.place.request.PlaceOrderRequestV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Currency;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaceOrdersControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to place an oder when no order lines were provided")
    void shouldAllowToPlaceTheOrderWhenNoOrderLineWereProvided() throws Exception {

        //setup: issuer service with an authorized response
        CorrelationId expectedCorrelationId = CorrelationId.random();
        TestMocks.setup(issuerService())
                .toAcceptTheAuthorizationWith(expectedCorrelationId);

        // given: a valid place order request
        var request = new PlaceOrderRequestV1(Currency.getInstance("USD"), 100L);

        // when: the place order request is made
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedCorrelationId) // distinct correlation ID for each request to ensure wiremock stubs are matched
                .content(objectMapper.writeValueAsString(request)));

        // then: the response is 201
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpectAll(jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.currency", is("USD")),
                        jsonPath("$.amount", is(100)));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to place an order when line items are provided")
    void shouldAllowToPlaceAnOrderWhenLineItemsAreProvided() throws Exception {

        //setup: issuer service with an authorized response
        CorrelationId expectedCorrelationId = CorrelationId.random();
        TestMocks.setup(issuerService())
                .toAcceptTheAuthorizationWith(expectedCorrelationId);

        // given: a valid place order request with line items (V2)
        var request = new PlaceOrderRequestV2(
                Currency.getInstance("USD"),
                List.of(
                        new LineItemRequest("Wireless Bluetooth Headphones", 2, 1000L, "SKU-HEADPHONE-001"),
                        new LineItemRequest("USB-C Charging Cable", 1, 2000L, "SKU-CABLE-002")
                ),
                null  // amount not provided, should be computed from line items
        );

        // when: the place order request is made (no version header defaults to V2)
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Correlation-ID", expectedCorrelationId)
                .content(objectMapper.writeValueAsString(request)));

        // then: the response is 201 with line items
        // Expected total: (2 * 1000) + (1 * 2000) = 4000
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.currency", is("USD")),
                        jsonPath("$.amount", is(4000)),
                        jsonPath("$.line_items", is(notNullValue())),
                        jsonPath("$.line_items.length()", is(2)),
                        jsonPath("$.line_items[0].name", is("Wireless Bluetooth Headphones")),
                        jsonPath("$.line_items[0].quantity", is(2)),
                        jsonPath("$.line_items[0].unit_price", is(1000)),
                        jsonPath("$.line_items[0].reference", is("SKU-HEADPHONE-001")),
                        jsonPath("$.line_items[1].name", is("USB-C Charging Cable")),
                        jsonPath("$.line_items[1].quantity", is(1)),
                        jsonPath("$.line_items[1].unit_price", is(2000)),
                        jsonPath("$.line_items[1].reference", is("SKU-CABLE-002"))
                );
    }
}
