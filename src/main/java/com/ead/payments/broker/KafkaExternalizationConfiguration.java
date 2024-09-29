package com.ead.payments.broker;

import com.ead.payments.BaseEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

@Configuration
public class KafkaExternalizationConfiguration {

    public static final String ORDERS_EVENTS_TOPIC = "orders-events.v1.topic";

 // Not needed for the task but it can be used if more fine grained control is needed
//    @Bean
//    EventExternalizationConfiguration eventExternalizationConfiguration() {
//
//        return EventExternalizationConfiguration.externalizing()
//                .select(EventExternalizationConfiguration.annotatedAsExternalized())
//                .route(
//                        BaseEvent.class,
//                        it -> RoutingTarget.forTarget(ORDERS_EVENTS_TOPIC).andKey(it.getId().toString())
//                )
//                .build();
//    }

}
