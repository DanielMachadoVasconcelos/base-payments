package com.ead.payments.orders;

import com.ead.payments.BaseEvent;
import com.ead.payments.EventProducer;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderEventProducer implements EventProducer {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void producer(String topic, BaseEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }
}
