package com.ead.payments.orders.search;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

class SearchOrderControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow to search an order by id when the order exists")
    void shouldAllowToSearchAnOrderByIdWhenTheOrderExists() throws Exception {
        // given: a valid place order request
        var request = new PlaceOrderRequest("USD", 100L);

        // and: the place order request is made
        var orderPlacedResponse = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .content(objectMapper.writeValueAsString(request))
        );

        // and: the order id is extracted from the response
        var orderId = objectMapper.readValue(orderPlacedResponse.andReturn().getResponse().getContentAsString(),
                PlaceOrderResponse.class).getId();

        // when: the search order request is made
        var response = mockMvc.perform(get(STR."/orders/\{orderId}")
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