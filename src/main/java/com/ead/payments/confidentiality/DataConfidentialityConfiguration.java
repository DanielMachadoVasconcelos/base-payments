package com.ead.payments.confidentiality;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ConditionalOnProperty(prefix = "spring.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataConfidentialityConfiguration {

    EntityManagerFactory entityManagerFactory;
    SensitiveDataMaskListener sensitiveDataMaskListener;

    @PostConstruct
    private void onApplicationStart() {

        log.info("Registering the confidentiality listener");
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PRE_LOAD).appendListener(sensitiveDataMaskListener);
    }
}
