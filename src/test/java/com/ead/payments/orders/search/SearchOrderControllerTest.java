package com.ead.payments.orders.search;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.ead.payments.orders.place.PlaceOrderRequest;
import com.ead.payments.orders.place.PlaceOrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Currency;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchOrderControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to search an order by id when the order exists")
    void shouldAllowToSearchAnOrderByIdWhenTheOrderExists() throws Exception {

        //setup: issuer service with an authorized response
        CorrelationId expectedCorrelationId = CorrelationId.random();
        TestMocks.setup(issuerService())
                .toAcceptTheAuthorizationWith(expectedCorrelationId);

        // given: a valid place order request
        var request = new PlaceOrderRequest(Currency.getInstance("USD"), 100L);

        // and: the place order request is made
        var orderPlacedResponse = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedCorrelationId)
                .content(objectMapper.writeValueAsString(request))
        );

        // and: the order id is extracted from the response
        var orderId = objectMapper.readValue(orderPlacedResponse.andReturn().getResponse().getContentAsString(),
                PlaceOrderResponse.class).getId();

        // when: the search order request is made
        var response = mockMvc.perform(get("/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
        );

        // then: the response is 200
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.currency", is("USD")),
                        jsonPath("$.amount", is(100))
                );
    }
}
