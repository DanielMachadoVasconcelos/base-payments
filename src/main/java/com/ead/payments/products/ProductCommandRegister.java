package com.ead.payments.products;

import com.ead.payments.eventsourcing.CommandDispatcher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ProductCommandRegister {

    CommandDispatcher commandDispatcher;
    CommandHandler commandHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void registerHandlers() {
        log.debug("Registering the Product commands handlers to the Commander Dispatcher!");
        commandDispatcher.registerHandler(CreateProductCommand.class, commandHandler::handler);
    }
}
