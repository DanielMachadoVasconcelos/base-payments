package com.ead.payments.orders.cancel;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.ead.payments.orders.place.response.PlaceOrderResponseV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Currency;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CancelOrderControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to cancel an order by id when the order exists")
    void shouldAllowToCancelAnOrderByIdWhenTheOrderExists() throws Exception {
        var orderId = placeOrder();

        // when: the cancel order request is made
        var response = mockMvc.perform(post("/orders/" + orderId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
        );

        // then: the response is 200
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.currency", is("USD")),
                        jsonPath("$.amount", is(100)),
                        jsonPath("$.status", is("CANCELLED")),
                        jsonPath("$.version", is(1))
                );
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should not cancel an order when the order is completed")
    void shouldNotCancelAnOrderWhenTheOrderIsCompleted() throws Exception {
        var orderId = placeOrder();

        mockMvc.perform(put("/orders/" + orderId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"))
                .andExpect(status().isOk());

        var cancelResponse = mockMvc.perform(post("/orders/" + orderId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        cancelResponse.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("The completed order with id " + orderId + " cannot be cancelled")));

        var searchResponse = mockMvc.perform(get("/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        searchResponse.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    private UUID placeOrder() throws Exception {
        //setup: issuer service with an authorized response
        CorrelationId expectedCorrelationId = CorrelationId.random();
        TestMocks.setup(issuerService())
                .toAcceptTheAuthorizationWith(expectedCorrelationId);

        // given: a valid place order request
        var request = new PlaceOrderRequestV1(Currency.getInstance("USD"), 100L);

        // and: the place order request is made
        var orderPlacedResponse = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedCorrelationId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // and: the order id is extracted from the response
        return objectMapper.readValue(orderPlacedResponse.andReturn().getResponse().getContentAsString(),
                PlaceOrderResponseV1.class).getId();
    }

}
