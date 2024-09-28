package com.ead.payments.broker;

import com.ead.payments.orders.OrderPlacedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    Binding binding(Queue queue, Exchange exchange) {
     return BindingBuilder.bind(queue).to(exchange).with("orders").noargs();
    }

    @Bean
    Queue queue() {
        return QueueBuilder.durable("orders").build();
    }

    @Bean
    Exchange exchange() {
        return ExchangeBuilder.topicExchange("orders").build();
    }
}
