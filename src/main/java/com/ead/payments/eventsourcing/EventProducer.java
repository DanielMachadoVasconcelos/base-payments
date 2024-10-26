package com.ead.payments.eventsourcing;

public interface EventProducer {

    void producer(String topic, BaseEvent event);
}
