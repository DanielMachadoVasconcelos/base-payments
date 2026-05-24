package com.ead.payments.providers;

import com.ead.payments.orders.cancel.CancelOrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class OrderCanceledProvider {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final OrderPlacedProvider orderPlacedProvider;

    public OrderCanceledProvider(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            OrderPlacedProvider orderPlacedProvider
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.orderPlacedProvider = orderPlacedProvider;
    }

    public CancelOrderResponse cancelOrder() throws Exception {
        var placedOrder = orderPlacedProvider.placeOrder();

        var orderCanceledResponse = mockMvc.perform(post("/orders/" + placedOrder.getId() + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0"))
                .andExpect(status().isOk());

        return objectMapper.readValue(
                orderCanceledResponse.andReturn().getResponse().getContentAsString(),
                CancelOrderResponse.class
        );
    }
}
