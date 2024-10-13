package com.ead.payments.orders;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class OrderEventHandler {

    @EventHandler
    public void on(OrderPlacedEvent event) {
        log.info("Handling the OrderPlacedEvent [{}]", event);
    }
}
