package com.ead.payments.orders.events;

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
    private UUID id;

    private Instant createdAt;

    private String eventType;

    private String eventData;
}
