package com.ead.payments.orders.place;

import com.ead.payments.SpringBootIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlaceInvalidOrderControllerTet extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("should not allow to place the order when the order is invalid")
    public void shouldNotAllowToPlaceTheOrderWhenTheOrderIsInvalid() throws Exception {

        // given: an invalid place order request
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                Currency.getInstance("USD"),
                -1200L
        );

        // when: the place order request is made
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .content(objectMapper.writeValueAsString(placeOrderRequest))
        );

        // then: the response is 400
        response.andDo(print())
                .andExpect(status().is4xxClientError());
    }

}
