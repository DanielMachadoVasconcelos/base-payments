package com.ead.payments.orders;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
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

@Sql(scripts = "/test-data/delete-all-orders.sql", executionPhase = AFTER_TEST_METHOD)
class OrdersControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should place orders successfully when line items are provided")
    void shouldPlaceOrdersSuccessfullyWhenLineItemsAreProvided() throws Exception {

        // Given
        var request = new PlaceOrderRequest(
                UUID.randomUUID(),
                1L,
                "USD",
                100L
        );

        // When
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(request))
        );

        // Then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.order_id").value(is(request.id().toString())),
                        jsonPath("$.currency").value(is("USD")),
                        jsonPath("$.amount").value(is(100)),
                        jsonPath("$.version").value(is(1))
                );
        ;
    }
}