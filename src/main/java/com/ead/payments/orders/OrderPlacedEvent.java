package com.ead.payments.orders;

import java.util.UUID;
import org.springframework.modulith.events.Externalized;

@Externalized(target = "orders")
public record OrderPlacedEvent(UUID orderId) {

}
