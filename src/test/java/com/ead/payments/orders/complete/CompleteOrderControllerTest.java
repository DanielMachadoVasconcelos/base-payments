package com.ead.payments.orders.complete;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.providers.OrderCanceledProvider;
import com.ead.payments.providers.OrderPlacedProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

class CompleteOrderControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderPlacedProvider orderPlacedProvider;

    @Autowired
    private OrderCanceledProvider orderCanceledProvider;

    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @Nested
    class ExistingOrderScenarios {

        private UUID orderId;

        @BeforeEach
        void placeOrderBeforeEachTest() throws Exception {
            // given: an authorized placed order exists for the customer
            orderId = orderPlacedProvider.placeOrder().getId();
        }

        @Test
        @DisplayName("Should allow to complete an order by id when the order exists")
        void shouldAllowToCompleteAnOrderByIdWhenTheOrderExists() throws Exception {
            // when: the customer completes the placed order
            var response = mockMvc.perform(put("/orders/" + orderId + "/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("version", "1.0.0"));

            // then: the order becomes completed and keeps its commercial details
            response.andDo(print())
                    .andExpect(status().isOk())
                    .andExpectAll(
                            jsonPath("$.id", is(orderId.toString())),
                            jsonPath("$.currency", is("USD")),
                            jsonPath("$.amount", is(100)),
                            jsonPath("$.status", is("COMPLETED")),
                            jsonPath("$.version", is(1))
                    );
        }

        @Test
        @DisplayName("Should return the same completed order when the order is already completed")
        void shouldReturnTheSameCompletedOrderWhenTheOrderIsAlreadyCompleted() throws Exception {
            // given: the order has already been completed once
            var firstResponse = mockMvc.perform(put("/orders/" + orderId + "/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("version", "1.0.0"));

            var completedOrder = objectMapper.readValue(
                    firstResponse.andReturn().getResponse().getContentAsString(),
                    CompleteOrderResponse.class
            );

            // when: the customer repeats the complete request
            var secondResponse = mockMvc.perform(put("/orders/" + orderId + "/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("version", "1.0.0"));

            // then: completion behaves idempotently and returns the same completed resource
            secondResponse.andDo(print())
                    .andExpect(status().isOk())
                    .andExpectAll(
                            jsonPath("$.id", is(completedOrder.getId().toString())),
                            jsonPath("$.currency", is(completedOrder.getCurrency().getCurrencyCode())),
                            jsonPath("$.amount", is(completedOrder.getAmount().intValue())),
                            jsonPath("$.status", is("COMPLETED")),
                            jsonPath("$.version", is(completedOrder.getVersion().intValue()))
                    );
        }
    }

    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @Nested
    class CanceledOrderScenarios {

        private UUID orderId;

        @BeforeEach
        void cancelOrderBeforeEachTest() throws Exception {
            // given: the order has already reached the cancelled terminal state
            orderId = orderCanceledProvider.cancelOrder().getId();
        }

        @Test
        @DisplayName("Should not complete an order when the order is cancelled")
        void shouldNotCompleteAnOrderWhenTheOrderIsCancelled() throws Exception {
            // when: the customer tries to complete the cancelled order
            var completeResponse = mockMvc.perform(put("/orders/" + orderId + "/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("version", "1.0.0"));

            // then: the domain rejects the invalid terminal-state transition
            completeResponse.andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail", is("The cancelled order with id " + orderId + " cannot be completed")));

            // and: the order remains cancelled
            var searchResponse = mockMvc.perform(get("/orders/" + orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("version", "1.0.0"));

            searchResponse.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("CANCELLED")));
        }
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("Should return not found when the order does not exist")
    void shouldReturnNotFoundWhenTheOrderDoesNotExist() throws Exception {
        // given: no order exists with the requested id
        var unknownOrderId = UUID.randomUUID();

        // when: the customer tries to complete that unknown order
        var response = mockMvc.perform(put("/orders/" + unknownOrderId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        // then: the API communicates that the order is not part of the system
        response.andDo(print())
                .andExpect(status().isNotFound());
    }
}
