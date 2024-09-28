package com.ead.payments;

public interface EventProducer {

    void producer(String topic, BaseEvent event);
}
