package com.ead.payments.json;

import com.ead.payments.orders.events.OrderEventResponse;
import com.ead.payments.orders.place.response.PlaceOrderResponseV2;
import com.ead.payments.orders.response.LineItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(JacksonConfiguration.class)
@TestPropertySource(properties = "spring.docker.compose.enabled=false")
class JacksonConfigurationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should serialize PlaceOrderResponseV2 with snake_case when Jackson configuration is applied")
    void shouldSerializePlaceOrderV2WithSnakeCaseWhenConfigured() {
        var response = new PlaceOrderResponseV2(
                UUID.randomUUID(),
                Currency.getInstance("USD"),
                4000L,
                List.of(new LineItemResponse("Headphone", 2, 1000L, "SKU-1"))
        );

        var json = objectMapper.valueToTree(response);

        assertThat(json.has("line_items")).isTrue();
        assertThat(json.has("lineItems")).isFalse();
        assertThat(json.get("line_items").get(0).has("unit_price")).isTrue();
        assertThat(json.get("line_items").get(0).has("unitPrice")).isFalse();
    }

    @Test
    @DisplayName("Should serialize order events with snake_case when Jackson configuration is applied")
    void shouldSerializeOrderEventsWithSnakeCaseWhenConfigured() {
        var response = new OrderEventResponse(
                UUID.randomUUID(),
                Instant.now(),
                "ORDER_PLACED",
                "{\"id\":\"abc\"}"
        );

        var json = objectMapper.valueToTree(response);

        assertThat(json.has("event_type")).isTrue();
        assertThat(json.has("event_data")).isTrue();
        assertThat(json.has("eventType")).isFalse();
        assertThat(json.has("eventData")).isFalse();
    }
}
