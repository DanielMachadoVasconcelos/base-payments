package com.ead.payments.orders.events;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.orders.place.PlaceOrderRequest;
import com.ead.payments.orders.place.PlaceOrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventsControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to retrieve all events for when reading an order history")
    void shouldAllowToRetrieveAllEventsForWhenReadingAnOrderHistory() throws Exception {
        // given: a valid place order request
        var request = new PlaceOrderRequest("USD", 100L);

        // and: the place order request is made
        var orderPlacedResponse = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedAuthorizedCorrelationId)
                .content(objectMapper.writeValueAsString(request))
        );

        // and: the order id is extracted from the response
        var orderId = objectMapper.readValue(orderPlacedResponse.andReturn().getResponse().getContentAsString(),
                PlaceOrderResponse.class).getId();

        // when: the get order events request is made
        var response = mockMvc.perform(get("/orders/" + orderId + "/events")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
        );

        // then: the response is 200
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(notNullValue())))
                .andExpect(jsonPath("$[0].eventType", is(notNullValue())))
                .andExpect(jsonPath("$[0].eventData", is(notNullValue())));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to retrieve a specific event for when reading an order history")
    void shouldAllowToRetrieveASpecificEventWhenReadingAnOrderHistory() throws Exception {
        // given: a valid place order request
        var request = new PlaceOrderRequest("USD", 100L);

        // and: the place order request is made
        var orderPlacedResponse = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedAuthorizedCorrelationId)
                .content(objectMapper.writeValueAsString(request))
        );

        // and: the order id is extracted from the response
        var orderId = objectMapper.readValue(orderPlacedResponse.andReturn().getResponse().getContentAsString(),
                PlaceOrderResponse.class).getId();

        // and: the get order events request is made to get the event id
        var eventsResponse = mockMvc.perform(get("/orders/" + orderId + "/events")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
        );

        // and: the event id is extracted from the response
        var eventId = objectMapper.readTree(eventsResponse.andReturn().getResponse().getContentAsString())
                .get(0).get("id").asText();

        // when: the get specific order event request is made
        var response = mockMvc.perform(get("/orders/" + orderId + "/events/" + eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
        );

        // then: the response is 200
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventId)))
                .andExpect(jsonPath("$.eventType", is(notNullValue())))
                .andExpect(jsonPath("$.eventData", is(notNullValue())));
    }
}
