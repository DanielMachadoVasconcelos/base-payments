package com.ead.payments.orders;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ead.payments.SpringBootIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Sql(scripts = "/test-data/delete-all-orders.sql", executionPhase = AFTER_TEST_CLASS)
class OrdersControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should place orders successfully when no line items are provided")
    void shouldPlaceOrdersSuccessfullyWhenLineItemsAreNotProvided() throws Exception {

        // Given: An order request with no line items
        var request = new PlaceOrderRequest(
                UUID.randomUUID(),
                1L,
                "USD",
                100L
        );

        // When placing an order
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(request))
        );

        // Then the order should be placed successfully
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.order_id").value(is(request.id().toString())),
                        jsonPath("$.currency").value(is("USD")),
                        jsonPath("$.amount").value(is(100)),
                        jsonPath("$.version").value(is(1))
                );
    }

    @Test
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should place orders successfully when no order identifier is provided")
    void shouldPlaceOrdersSuccessfullyWhenNoOrderIdWasProvided() throws Exception {

        // Given: An order request with no line items
        var request = new PlaceOrderRequest(
                null,
                1L,
                "USD",
                100L
        );

        // When placing an order
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(request))
        );

        // Then the order should be placed successfully
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.order_id").value(notNullValue()),
                        jsonPath("$.currency").value(is("USD")),
                        jsonPath("$.amount").value(is(100)),
                        jsonPath("$.version").value(is(1))
                );
    }

    @Test
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should not allow to place order  when currency is missing")
    void shouldNotAllowToPlaceOrderWhenCurrencyIsMissing() throws Exception {

        // Given: An order request with no line items
        var request = new PlaceOrderRequest(
                UUID.randomUUID(),
                1L,
                null,
                100L
        );

        // When placing an order
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(request))
        );

        // Then the order should not be placed successfully
        response.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpectAll(
                        jsonPath("$.details").value(is("The 'PlaceOrderRequest' is invalid")),
                        jsonPath("$.invalid_params.currency").value(is("must not be blank")),
                        jsonPath("$.title").value(is("Constraint Violation Exception"))
                );
    }

    @Test
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should not allow to place order  when amount is missing")
    void shouldNotAllowToPlaceOrderWhenAmountIsMissing() throws Exception {

        // Given: An order request with no line items
        var request = new PlaceOrderRequest(
                UUID.randomUUID(),
                1L,
                "USD",
                null
        );

        // When placing an order
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(request))
        );

        // Then the order should not be placed successfully
        response.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpectAll(
                        jsonPath("$.details").value(is("The 'PlaceOrderRequest' is invalid")),
                        jsonPath("$.invalid_params.amount").value(is("must not be null")),
                        jsonPath("$.title").value(is("Constraint Violation Exception"))
                );
    }

}