package com.ead.payments.observability;

import io.micrometer.observation.ObservationPredicate;
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
    void shouldIgnoreActuatorServerRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        ServerRequestObservationContext context =
                new ServerRequestObservationContext(request, new MockHttpServletResponse());

        assertThat(this.predicate.test("http.server.requests", context)).isFalse();
    }

    @Test
    void shouldKeepBusinessServerRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");
        ServerRequestObservationContext context =
                new ServerRequestObservationContext(request, new MockHttpServletResponse());
        context.setPathPattern("/orders");

        assertThat(this.predicate.test("http.server.requests", context)).isTrue();
    }

    @Test
    void shouldIgnoreEurekaClientRequests() {
        MockClientHttpRequest request =
                new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost:8761/eureka/apps/BASE-PAYMENTS"));
        ClientRequestObservationContext context = new ClientRequestObservationContext(request);

        assertThat(this.predicate.test("http.client.requests", context)).isFalse();
    }

    @Test
    void shouldKeepBusinessClientRequests() {
        MockClientHttpRequest request =
                new MockClientHttpRequest(HttpMethod.POST, URI.create("http://localhost:18081/authorization"));
        ClientRequestObservationContext context = new ClientRequestObservationContext(request);

        assertThat(this.predicate.test("http.client.requests", context)).isTrue();
    }
}
