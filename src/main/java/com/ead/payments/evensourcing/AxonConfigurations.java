package com.ead.payments.evensourcing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Log4j2
@Configuration
public class AxonConfigurations {

    @Bean
    public CommandGateway commandGateway(CommandBus commandBus) {
        return DefaultCommandGateway.builder()
                .commandBus(commandBus)
                .build();
    }

    @Bean
    @Primary
    public Serializer serializer(ObjectMapper objectMapper) {
        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .build();
    }

    public AxonConfigurations(EventProcessingConfigurer configurer) {
        // Configure a Subscribing Event Processor
        configurer.usingSubscribingEventProcessors();

        // Configure a Tracking Event Processor
        configurer.registerTrackingEventProcessorConfiguration(
                config -> TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
        );
    }
}