package com.ead.payments.orders.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for order events from the event_publication table.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_publication", schema = "orders")
public class OrderEvent {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "publication_date", nullable = false)
    private Instant createdAt;

    @Column(name = "listener_id", nullable = false)
    private String listenerId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "serialized_event", nullable = false)
    private String serializedEvent;

    @Column(name = "completion_date")
    private Instant completionDate;

    /**
     * Gets the event data from the serialized event.
     * 
     * @return the event data
     */
    public String getEventData() {
        return serializedEvent;
    }
}
