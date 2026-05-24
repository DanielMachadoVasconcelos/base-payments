package com.ead.payments.orders.complete;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.ead.payments.orders.place.response.PlaceOrderResponseV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("Should allow to complete an order by id when the order exists")
    void shouldAllowToCompleteAnOrderByIdWhenTheOrderExists() throws Exception {
        var orderId = placeOrder();

        var response = mockMvc.perform(put("/orders/" + orderId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

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
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("Should return the same completed order when the order is already completed")
    void shouldReturnTheSameCompletedOrderWhenTheOrderIsAlreadyCompleted() throws Exception {
        var orderId = placeOrder();

        var firstResponse = mockMvc.perform(put("/orders/" + orderId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        var completedOrder = objectMapper.readValue(
                firstResponse.andReturn().getResponse().getContentAsString(),
                CompleteOrderResponse.class
        );

        var secondResponse = mockMvc.perform(put("/orders/" + orderId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

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

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("Should not complete an order when the order is cancelled")
    void shouldNotCompleteAnOrderWhenTheOrderIsCancelled() throws Exception {
        var orderId = placeOrder();

        mockMvc.perform(post("/orders/" + orderId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"))
                .andExpect(status().isOk());

        var completeResponse = mockMvc.perform(put("/orders/" + orderId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        completeResponse.andDo(print())
                .andExpect(status().isConflict());

        var searchResponse = mockMvc.perform(get("/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        searchResponse.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("Should return not found when the order does not exist")
    void shouldReturnNotFoundWhenTheOrderDoesNotExist() throws Exception {
        var response = mockMvc.perform(put("/orders/" + UUID.randomUUID() + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    private UUID placeOrder() throws Exception {
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
