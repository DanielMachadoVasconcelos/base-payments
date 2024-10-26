package com.ead.payments.orders;

import com.ead.payments.eventsourcing.EventSourcingHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderCommandHandler  implements CommandHandler {

    EventSourcingHandler<OrderAggregate> eventSourcingHandler;

    public void handler(PlaceOrderCommand command) {
        var aggregate = new OrderAggregate(command);
        eventSourcingHandler.save(aggregate);
    }
}
