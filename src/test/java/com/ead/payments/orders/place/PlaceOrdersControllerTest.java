package com.ead.payments.orders.place;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ead.payments.SpringBootIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

class PlaceOrdersControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to place an oder when no order lines were provided")
    void shouldAllowToPlaceTheOrderWhenNoOrderLineWereProvided() throws Exception {

        // given: a valid place order request
        var request = new PlaceOrderRequest("USD", 100L);

        // when: the place order request is made
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedAuthorizedCorrelationId) // distinct correlation ID for each request to ensure wiremock stubs are matched
                .content(objectMapper.writeValueAsString(request)));

        // then: the response is 201
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpectAll(jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.currency", is("USD")),
                        jsonPath("$.amount", is(100)));
    }
}