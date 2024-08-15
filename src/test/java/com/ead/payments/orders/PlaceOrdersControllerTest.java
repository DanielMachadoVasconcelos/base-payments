package com.ead.payments.orders;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ead.payments.SpringBootIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
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
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should place orders successfully when no line items are provided")
    void shouldPlaceOrdersSuccessfullyWhenLineItemsAreNotProvided() throws Exception {

        // Given: An order request with no line items
        var request = new PlaceOrderRequest(
                UUID.randomUUID(),
                1L,
                "USD",
                100L,
                Set.of()
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
                100L,
                Set.of()
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
                100L,
                Set.of()
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
                        jsonPath("$.detail").value(is("The 'PlaceOrderRequest' is invalid")),
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
                null,
                Set.of()
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
                        jsonPath("$.detail").value(is("The 'PlaceOrderRequest' is invalid")),
                        jsonPath("$.invalid_params.amount").value(is("must not be null")),
                        jsonPath("$.title").value(is("Constraint Violation Exception"))
                );
    }

    @Test
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should allow to update order amount when value is grater then zero")
    void shouldAllowToUpdateOrderAmountWhenValueIsGraterThenZero() throws Exception {

        // Setup: An order request with no line items
        var placeOrderRequest = new PlaceOrderRequest(
                UUID.randomUUID(),
                1L,
                "USD",
                2000L,
                Set.of()
        );

        // And:  the order is placed successfully
       mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(placeOrderRequest))
        )
               .andExpect(status().isCreated());

        // Given: An order request with no line items
        var updateOrderRequest = new UpdateOrderRequest(
                1L,
                100L
        );

        // When: placing an order
        var response = mockMvc.perform(patch("/orders/%s".formatted(placeOrderRequest.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(updateOrderRequest))
        );

        // Then: the order should be updated successfully
        response.andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpectAll(
                        jsonPath("$.id").value(is(placeOrderRequest.id().toString())),
                        jsonPath("$.amount").value(is(100)),
                        jsonPath("$.version").value(is(1))
                );

        // And: the order should have the new version and amount
        mockMvc.perform(get("/orders/%s".formatted(placeOrderRequest.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(placeOrderRequest))
        )
                .andExpect(status().is2xxSuccessful())
                .andExpectAll(
                        jsonPath("$.amount").value(is(100)),
                        jsonPath("$.version").value(is(2))
                );
    }
}