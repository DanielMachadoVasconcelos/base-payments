package com.ead.payments.orders.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for retrieving order events from the event_publication table.
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderEventService {

    private final OrderEventRepository orderEventRepository;

    /**
     * Retrieves all events for an order.
     *
     * @param orderId the ID of the order
     * @return a list of events for the order
     */
    public List<OrderEventResponse> findAllByOrderId(UUID orderId) {
        log.info("Finding all events for order: {}", orderId);

        // First, try to find all events to see if any exist
        List<OrderEvent> allEvents = orderEventRepository.findAllEvents();
        log.info("Found {} events in total", allEvents.size());
        for (OrderEvent event : allEvents) {
            log.info("Event: id={}, type={}, serializedEvent={}", 
                    event.getId(), event.getEventType(), event.getSerializedEvent());
        }

        // Then, try to find events for the specific order
        List<OrderEvent> orderEvents = orderEventRepository.findByOrderId(orderId.toString());
        log.info("Found {} events for order {}", orderEvents.size(), orderId);

        return orderEvents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific event for an order.
     *
     * @param orderId the ID of the order
     * @param eventId the ID of the event
     * @return the event, or empty if not found
     */
    public Optional<OrderEventResponse> findByOrderIdAndEventId(UUID orderId, UUID eventId) {
        log.info("Finding event {} for order: {}", eventId, orderId);
        return orderEventRepository.findByOrderIdAndEventId(orderId.toString(), eventId)
                .map(this::mapToResponse);
    }

    /**
     * Maps an OrderEvent entity to an OrderEventResponse DTO.
     *
     * @param event the OrderEvent entity
     * @return the OrderEventResponse DTO
     */
    private OrderEventResponse mapToResponse(OrderEvent event) {
        return new OrderEventResponse(
                event.getId(),
                event.getCreatedAt(),
                event.getEventType(),
                event.getEventData()
        );
    }
}
