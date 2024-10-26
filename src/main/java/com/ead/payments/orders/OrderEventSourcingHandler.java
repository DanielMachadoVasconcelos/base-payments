package com.ead.payments.orders;

import com.ead.payments.eventsourcing.AggregateRoot;
import com.ead.payments.eventsourcing.BaseEvent;
import com.ead.payments.eventsourcing.EventSourcingHandler;
import com.ead.payments.eventsourcing.EventStore;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@AllArgsConstructor
public class OrderEventSourcingHandler implements EventSourcingHandler<OrderAggregate> {

    EventStore eventStore;

    @Override
    public void save(OrderAggregate aggregate) {
        eventStore.saveEvents(aggregate.getId(), aggregate.getUncommittedChanges(), aggregate.getVersion());
        aggregate.markChangesAsCommitted();
    }

    @Override
    public OrderAggregate getById(UUID id) {
        var aggregate = new OrderAggregate();
        var events = eventStore.getEvents(id);
        if(events != null && !events.isEmpty()) {
            aggregate.replayEvents(events);
            var latestVersion = events.stream().map(BaseEvent::getVersion).max(Comparator.naturalOrder());
            aggregate.setVersion(latestVersion.get());
        }
        return aggregate;
    }
}
