package com.ead.payments.orders.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for order events from the event_publication table.
 */
@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, UUID> {

    /**
     * Find all events.
     *
     * @return a list of all events
     */
    @Query(value = "SELECT * FROM orders.event_publication", nativeQuery = true)
    List<OrderEvent> findAllEvents();

    /**
     * Find all events for an order.
     *
     * @param orderId the ID of the order
     * @return a list of events for the order
     */
    @Query(value = "SELECT * FROM orders.event_publication WHERE serialized_event LIKE CONCAT('%', :orderId, '%')", nativeQuery = true)
    List<OrderEvent> findByOrderId(@Param("orderId") String orderId);

    /**
     * Find a specific event for an order.
     *
     * @param orderId the ID of the order
     * @param eventId the ID of the event
     * @return the event, or empty if not found
     */
    @Query(value = "SELECT * FROM orders.event_publication WHERE serialized_event LIKE CONCAT('%', :orderId, '%') AND id = :eventId", nativeQuery = true)
    Optional<OrderEvent> findByOrderIdAndEventId(@Param("orderId") String orderId, @Param("eventId") UUID eventId);
}
