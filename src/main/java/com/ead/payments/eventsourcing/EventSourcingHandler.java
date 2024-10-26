package com.ead.payments.eventsourcing;

import java.util.UUID;

public interface EventSourcingHandler<T extends AggregateRoot> {

    void save(T aggregate);
    T getById(UUID id);
}
