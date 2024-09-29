package com.ead.payments.orders;

import com.ead.payments.CommandDispatcher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class OrderCommandRegister {

    CommandDispatcher commandDispatcher;
    CommandHandler commandHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void registerHandlers() {
        log.debug("Registering the Account commands handlers to the Commander Dispatcher!");
        commandDispatcher.registerHandler(PlaceOrderCommand.class, commandHandler::handler);
    }
}
