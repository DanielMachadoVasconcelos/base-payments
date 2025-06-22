package com.ead.payments.orders.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;


@Value
@Entity
@RequiredArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Table(name = "event_publication", schema = "orders")
public class OrderEvent {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "publication_date", nullable = false)
    Instant createdAt;

    @Column(name = "listener_id", nullable = false)
    String listenerId;

    @Column(name = "event_type", nullable = false)
    String eventType;

    @Column(name = "serialized_event", nullable = false)
    String serializedEvent;

    @Column(name = "completion_date")
    Instant completionDate;

    /**
     * Gets the event data from the serialized event.
     * 
     * @return the event data
     */
    public String getEventData() {
        return serializedEvent;
    }
}
