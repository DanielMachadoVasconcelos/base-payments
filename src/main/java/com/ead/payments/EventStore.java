package com.ead.payments;

import java.util.List;
import java.util.UUID;

public interface EventStore {
    void saveEvents(UUID aggregatedId, Iterable<BaseEvent> events, int expectedVersion);
    List<BaseEvent> getEvents(UUID aggregatedId);
}
