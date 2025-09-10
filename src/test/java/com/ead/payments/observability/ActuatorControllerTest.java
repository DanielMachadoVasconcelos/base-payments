package com.ead.payments.observability;

import com.ead.payments.SpringBootIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "engineer", roles = "ADMIN")
public class ActuatorControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should expose the actuator health endpoint when application is running")
    void shouldExposeTheActuatorHealthEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/health").with(httpBasic("GRAFANA", "grafana")));

        // then the response is successful
        response
                .andExpect(status().isOk())
                .andDo(print());

        // and the response contains the status up
        response
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Should expose the actuator info endpoint when application is running")
    void shouldExposeTheActuatorInfoEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/info").with(httpBasic("GRAFANA", "grafana")));

        // then the response is successful
        response
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("Should expose the actuator metrics endpoint when application is running")
    void shouldExposeTheActuatorMetricsEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/metrics").with(httpBasic("GRAFANA", "grafana")));

        // then the response is successful
        response
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("Should expose the actuator prometheus endpoint when application is running")
    void shouldExposeTheActuatorPrometheusEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/prometheus").with(httpBasic("GRAFANA", "grafana")));

        // then the response is successful and returns text/plain in Prometheus format
        response
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Type", org.hamcrest.Matchers.containsString("text/plain")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("jvm_info")));
    }

    @Test
    @DisplayName("Should expose 'issuer.authorize' metric when invoking authorization")
    void shouldExposeIssuerAuthorizeMetricWhenAfterInvokingAuthorization() throws Exception {
        // setup: stub issuer service to approve authorization
        var correlationId = com.ead.payments.logging.CorrelationId.random();
        com.ead.payments.mocks.TestMocks.setup(issuerService())
                .toAcceptTheAuthorizationWith(correlationId);

        // trigger the metric by placing an order
        var request = new com.ead.payments.orders.place.PlaceOrderRequest("USD", 100L);
        mockMvc.perform(
                post("/orders")
                        .with(httpBasic("merchant", "password"))
                        .header("version", "1.0.0")
                        .header("X-Correlation-ID", correlationId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());

        // verify the specific metric endpoint shows measurements
        ResultActions metricsResponse = mockMvc.perform(get("/actuator/metrics/issuer.authorize")
                .with(httpBasic("GRAFANA", "grafana")));

        metricsResponse
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("issuer.authorize")))
                .andExpect(jsonPath("$.measurements", is(notNullValue())));
    }
}
