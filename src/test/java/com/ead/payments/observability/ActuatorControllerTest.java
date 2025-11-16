package com.ead.payments.observability;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Currency;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ActuatorControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "engineer", roles = "ADMIN")
    @DisplayName("Should expose the actuator health endpoint when application is running")
    void shouldExposeTheActuatorHealthEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/health"));

        // then the response is successful
        response
                .andExpect(status().isOk())
                .andDo(print());

        // and the response contains the status up
        response
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @WithMockUser(username = "engineer", roles = "ADMIN")
    @DisplayName("Should expose the actuator info endpoint when application is running")
    void shouldExposeTheActuatorInfoEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/info"));

        // then the response is successful
        response
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "engineer", roles = "ADMIN")
    @DisplayName("Should expose the actuator metrics endpoint when application is running")
    void shouldExposeTheActuatorMetricsEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/metrics"));

        // then the response is successful
        response
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "engineer", roles = "ADMIN")
    @DisplayName("Should expose the actuator prometheus endpoint when application is running")
    void shouldExposeTheActuatorPrometheusEndpointWhenApplicationIsRunning() throws Exception {
        // given the actuator endpoint is invoked
        ResultActions response = mockMvc.perform(get("/actuator/prometheus"));

        // then the response is successful and returns text/plain in Prometheus format
        response
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", Matchers.containsString("text/plain")))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("jvm_info")));
    }

    @Test
    @WithMockUser(username = "engineer", roles = "ADMIN")
    @DisplayName("Should expose 'issuer.authorize' metric on actuator endpoint when invoking authorization")
    void shouldExposeIssuerAuthorizeMetricWhenAfterInvokingAuthorization() throws Exception {

        // setup: stub issuer service to approve authorization
        var correlationId = CorrelationId.random();
        TestMocks.setup(issuerService())
                .toAcceptTheAuthorizationWith(correlationId);

        // given: trigger the metric by placing an order
        var request = new PlaceOrderRequestV1(Currency.getInstance("USD"), 100L);
        mockMvc.perform(
                        post("/orders")
                                .header("version", "1.0.0")
                                .header("X-Correlation-ID", correlationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        // when: verify the specific metric endpoint shows measurements
        mockMvc.perform(get("/actuator/metrics/issuer.authorize"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("issuer.authorize")))
                .andExpect(jsonPath("$.measurements", is(notNullValue())));
    }
}
