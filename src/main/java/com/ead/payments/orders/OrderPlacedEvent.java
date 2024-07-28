package com.ead.payments.orders;

import org.springframework.modulith.events.Externalized;

@Externalized(target = "orders")
public record OrderPlacedEvent(Integer orderId) {

}
