package com.ead.payments.orders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;

public class SearchOrdersControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "merchant", roles = {"MERCHANT"})
    @DisplayName("Should be able to find an order by id successfully when providing a valid order id")
    void shouldBeAbleToFindAnOrderByIdSuccessfullyWhenProvidingAValidOrderId() throws Exception {

        // Given an order id
       var expectedOrderId = UUID.randomUUID();
       var request = new PlaceOrderRequest(expectedOrderId, 1L, "USD", 100L);

        // and: the order is created successfully
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When searching for an order by id
        var response = mockMvc.perform(get(STR."/orders/\{expectedOrderId}")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"));

        // Then the order should be returned successfully
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id").value(expectedOrderId.toString()),
                        jsonPath("$.version").value(1),
                        jsonPath("$.currency").value("USD"),
                        jsonPath("$.amount").value(100)
                );
    }
}
