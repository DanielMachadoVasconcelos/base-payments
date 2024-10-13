package com.ead.payments.broker;

import org.springframework.context.annotation.Configuration;

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
