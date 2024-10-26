package com.ead.payments.products;

import com.ead.payments.eventsourcing.BaseEvent;
import com.ead.payments.eventsourcing.EventSourcingHandler;
import com.ead.payments.eventsourcing.EventStore;
import java.util.Comparator;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductEventSourcingHandler implements EventSourcingHandler<ProductAggregate> {

    EventStore eventStore;

    @Override
    public void save(ProductAggregate aggregate) {
        eventStore.saveEvents(aggregate.getId(), aggregate.getUncommittedChanges(), aggregate.getVersion());
        aggregate.markChangesAsCommitted();
    }

    @Override
    public ProductAggregate getById(UUID id) {
        var aggregate = new ProductAggregate();
        var events = eventStore.getEvents(id);
        if(events != null && !events.isEmpty()) {
            aggregate.replayEvents(events);
            var latestVersion = events.stream().map(BaseEvent::getVersion).max(Comparator.naturalOrder());
            aggregate.setVersion(latestVersion.get());
        }
        return aggregate;
    }
}