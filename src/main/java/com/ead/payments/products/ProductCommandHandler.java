package com.ead.payments.products;

import com.ead.payments.eventsourcing.EventSourcingHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductCommandHandler implements CommandHandler {

    EventSourcingHandler<ProductAggregate> eventSourcingHandler;

    public void handler(CreateProductCommand command) {
        var aggregate = new ProductAggregate(command);
        eventSourcingHandler.save(aggregate);
    }
}
