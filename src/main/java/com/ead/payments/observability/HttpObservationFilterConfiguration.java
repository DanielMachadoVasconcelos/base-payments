package com.ead.payments.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
class HttpObservationFilterConfiguration {

    @Bean
    ObservationPredicate noisyHttpObservationPredicate() {
        return (name, context) -> !isIgnoredHttpObservation(context);
    }

    private boolean isIgnoredHttpObservation(Observation.Context context) {
        if (context instanceof ServerRequestObservationContext serverContext) {
            return isIgnoredServerRequest(serverContext);
        }
        if (context instanceof ClientRequestObservationContext clientContext) {
            return isIgnoredClientRequest(clientContext);
        }
        return false;
    }

    private boolean isIgnoredServerRequest(ServerRequestObservationContext context) {
        String path = context.getPathPattern();
        if (path == null && context.getCarrier() != null) {
            path = context.getCarrier().getRequestURI();
        }
        return path != null && path.startsWith("/actuator");
    }

    private boolean isIgnoredClientRequest(ClientRequestObservationContext context) {
        ClientHttpRequest request = context.getCarrier();
        if (request == null || request.getURI() == null) {
            return false;
        }
        String path = request.getURI().getPath();
        return path != null && path.startsWith("/eureka/");
    }
}
