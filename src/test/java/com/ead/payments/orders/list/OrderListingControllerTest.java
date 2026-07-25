package com.ead.payments.orders.list;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.providers.OrderPlacedProvider;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderListingControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderPlacedProvider orderPlacedProvider;

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("Should return a paginated collection when orders exist")
    void shouldReturnAPaginatedCollectionWhenOrdersExist() throws Exception {
        // given: an order has been placed by a customer
        var placedOrder = orderPlacedProvider.placeOrder();

        // when: the customer browses the first page of orders
        var response = mockMvc.perform(get("/orders")
                .header("version", "1.0.0")
                .queryParam("page", "0")
                .queryParam("size", "1"));

        // then: the collection exposes stable pagination metadata
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(placedOrder.getId().toString())))
                .andExpect(jsonPath("$.content[0].created_at").isNotEmpty())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.total_elements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.total_pages", greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser(username = "merchant", roles = "MERCHANT")
    @DisplayName("Should apply lifecycle and period filters when browsing orders")
    void shouldApplyLifecycleAndPeriodFiltersWhenBrowsingOrders() throws Exception {
        // given: at least one placed order exists
        orderPlacedProvider.placeOrder();

        // when: the merchant filters orders by their lifecycle and a past lower bound
        var response = mockMvc.perform(get("/orders")
                .queryParam("status", "PLACED")
                .queryParam("created_from", Instant.parse("2000-01-01T00:00:00Z").toString())
                .queryParam("size", "100"));

        // then: every returned order satisfies the requested lifecycle
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("PLACED"))));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("Should reject an inverted period when browsing orders")
    void shouldRejectAnInvertedPeriodWhenBrowsingOrders() throws Exception {
        // given: a period whose start is later than its end
        var createdFrom = "2026-07-23T00:00:00Z";
        var createdTo = "2026-07-22T00:00:00Z";

        // when: the customer tries to browse orders in that period
        var response = mockMvc.perform(get("/orders")
                .queryParam("created_from", createdFrom)
                .queryParam("created_to", createdTo));

        // then: the invalid business query is rejected explicitly
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Invalid Order Listing Period")));
    }
}
