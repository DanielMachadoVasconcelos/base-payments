package com.ead.payments.orders.cancel;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.providers.OrderPlacedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "customer", roles = "CUSTOMER")
class CancelOrderControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderPlacedProvider orderPlacedProvider;

    private UUID orderId;

    @BeforeEach
    void placeOrderBeforeEachTest() throws Exception {
        // given: an authorized placed order exists for the customer
        orderId = orderPlacedProvider.placeOrder().getId();
    }

    @Test
    @DisplayName("Should allow to cancel an order by id when the order exists")
    void shouldAllowToCancelAnOrderByIdWhenTheOrderExists() throws Exception {
        // when: the customer cancels the placed order
        var response = mockMvc.perform(post("/orders/" + orderId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
        );

        // then: the order becomes cancelled and keeps its commercial details
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
    @DisplayName("Should not cancel an order when the order is completed")
    void shouldNotCancelAnOrderWhenTheOrderIsCompleted() throws Exception {
        // given: the order has already reached the completed terminal state
        mockMvc.perform(put("/orders/" + orderId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"))
                .andExpect(status().isOk());

        // when: the customer tries to cancel the completed order
        var cancelResponse = mockMvc.perform(post("/orders/" + orderId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        // then: the domain rejects the invalid terminal-state transition
        cancelResponse.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("The completed order with id " + orderId + " cannot be cancelled")));

        // and: the order remains completed
        var searchResponse = mockMvc.perform(get("/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        searchResponse.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

}
