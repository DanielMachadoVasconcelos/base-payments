package com.ead.payments.orders.events;

import com.ead.payments.logging.OrderIdLoggingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for order events.
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/orders")
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class EventsController {

    private final OrderEventService orderEventService;

    /**
     * Retrieves all events for an order.
     *
     * @param orderId the ID of the order
     * @return a list of events for the order
     */
    @GetMapping(path = "/{order_id}/events", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderEventResponse> getOrderEvents(@PathVariable("order_id") @NotNull UUID orderId) {
        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(orderId)) {
            log.info("Retrieving events for order: {}", orderId);

            List<OrderEventResponse> events = orderEventService.findAllByOrderId(orderId);
            log.info("Found {} events from service", events.size());
            return events;
        }
    }

    /**
     * Retrieves a specific event for an order.
     *
     * @param orderId the ID of the order
     * @param eventId the ID of the event
     * @return the event
     */
    @GetMapping(path = "/{order_id}/events/{event_id}", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    public OrderEventResponse getOrderEvent(
            @PathVariable("order_id") @NotNull UUID orderId,
            @PathVariable("event_id") @NotNull UUID eventId) {
        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(orderId)) {
            log.info("Retrieving event {} for order: {}", eventId, orderId);

            return orderEventService.findByOrderIdAndEventId(orderId, eventId)
                    .orElseThrow(() -> new OrderEventNotFoundException(orderId, eventId));
        }
    }
}
