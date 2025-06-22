package com.ead.payments.orders.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for order events.
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventResponse {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("eventData")
    private String eventData;
}
