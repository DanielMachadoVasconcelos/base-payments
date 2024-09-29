package com.ead.payments.orders;

import com.ead.payments.AggregateRoot;
import com.ead.payments.EventSourcingHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderCommandHandler implements CommandHandler {

    EventSourcingHandler<AggregateRoot> eventSourcingHandler;

    @Override
    public void handler(PlaceOrderCommand command) {
        var aggregate = new OrderAggregate(command);
        eventSourcingHandler.save(aggregate);
    }
}
