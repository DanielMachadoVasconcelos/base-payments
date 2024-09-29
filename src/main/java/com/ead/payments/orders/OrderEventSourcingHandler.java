package com.ead.payments.orders;

import com.ead.payments.AggregateRoot;
import com.ead.payments.BaseEvent;
import com.ead.payments.EventSourcingHandler;
import com.ead.payments.EventStore;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@AllArgsConstructor
public class OrderEventSourcingHandler implements EventSourcingHandler<AggregateRoot> {

    EventStore eventStore;

    @Override
    public void save(AggregateRoot aggregate) {
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
