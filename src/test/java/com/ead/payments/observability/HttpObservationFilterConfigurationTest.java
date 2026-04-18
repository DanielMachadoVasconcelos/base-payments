package com.ead.payments.observability;

import io.micrometer.observation.ObservationPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class HttpObservationFilterConfigurationTest {

    private final ObservationPredicate predicate =
            new HttpObservationFilterConfiguration().noisyHttpObservationPredicate();

    @Test
    @DisplayName("Should ignore actuator server requests when the path starts with /actuator")
    void shouldIgnoreActuatorServerRequestsWhenPathStartsWithActuator() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        ServerRequestObservationContext context =
                new ServerRequestObservationContext(request, new MockHttpServletResponse());

        assertThat(this.predicate.test("http.server.requests", context)).isFalse();
    }

    @Test
    @DisplayName("Should keep business server requests when the path belongs to the application")
    void shouldKeepBusinessServerRequestsWhenPathIsApplicationEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");
        ServerRequestObservationContext context =
                new ServerRequestObservationContext(request, new MockHttpServletResponse());
        context.setPathPattern("/orders");

        assertThat(this.predicate.test("http.server.requests", context)).isTrue();
    }

    @Test
    @DisplayName("Should ignore Eureka client requests when the target path starts with /eureka")
    void shouldIgnoreEurekaClientRequestsWhenPathStartsWithEureka() {
        MockClientHttpRequest request =
                new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost:8761/eureka/apps/BASE-PAYMENTS"));
        ClientRequestObservationContext context = new ClientRequestObservationContext(request);

        assertThat(this.predicate.test("http.client.requests", context)).isFalse();
    }

    @Test
    @DisplayName("Should keep business client requests when the target dependency is part of the business flow")
    void shouldKeepBusinessClientRequestsWhenPathTargetsBusinessDependency() {
        MockClientHttpRequest request =
                new MockClientHttpRequest(HttpMethod.POST, URI.create("http://localhost:18081/authorization"));
        ClientRequestObservationContext context = new ClientRequestObservationContext(request);

        assertThat(this.predicate.test("http.client.requests", context)).isTrue();
    }
}
