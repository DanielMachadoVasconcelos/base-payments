package com.ead.payments.orders;

import com.ead.payments.eventsourcing.AggregateNotFoundException;
import com.ead.payments.eventsourcing.ConcurrencyException;
import com.ead.payments.eventsourcing.BaseEvent;
import com.ead.payments.eventsourcing.EventModel;
import com.ead.payments.eventsourcing.EventStore;
import com.ead.payments.eventsourcing.EventStoreRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class OrderEventStore implements EventStore {

    private EventStoreRepository repository;
    private ApplicationEventPublisher producer;

    @Override
    public void saveEvents(UUID aggregatedId, Iterable<BaseEvent> events, int expectedVersion) {

        var eventStream = repository.findByAggregatedIdentifier(aggregatedId.toString());
        int latestVersion = eventStream.stream()
                .map(EventModel::getVersion)
                .max(Comparator.naturalOrder())
                .orElse(-1);

        if (expectedVersion != 1 && latestVersion != expectedVersion) {
            var errorMessage = MessageFormat.format("Mismatch on the expected version. Current version is {0}", latestVersion);
            throw new ConcurrencyException(errorMessage);
        }

        var version = expectedVersion;
        for (var event : events) {
            version++;
            event.setVersion(version);
            var eventModel = EventModel.builder()
                    .id(UUID.randomUUID().toString())
                    .createdAt(Date.from(Instant.now()))
                    .aggregatedIdentifier(aggregatedId.toString())
                    .aggregateType(OrderAggregate.class.getTypeName())
                    .version(version)
                    .eventType(event.getClass().getTypeName())
                    .eventData(event)
                    .build();

            var persistedEvent = repository.save(eventModel);
            if (!persistedEvent.getId().isEmpty()) {
                producer.publishEvent(event);
            }
        }
    }

    @Override
    public List<BaseEvent> getEvents(UUID aggregatedId) {
        var eventStream = repository.findByAggregatedIdentifier(aggregatedId.toString());
        if (eventStream == null || eventStream.isEmpty()) {
            throw new AggregateNotFoundException(MessageFormat.format("Incorrect account ID provided! No bank account found for ID {0}", aggregatedId));
        }
        return eventStream.stream()
                .map(EventModel::getEventData)
                .sorted(Comparator.comparing(BaseEvent::getVersion))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}

