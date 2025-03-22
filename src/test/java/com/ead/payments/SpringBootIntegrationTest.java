package com.ead.payments;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.observation.tck.TestObservationRegistry;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Execution(ExecutionMode.CONCURRENT)
@EnableWireMock({@ConfigureWireMock(name = "issuer-service", baseUrlProperties = "localhost", port = 6580)})
public class SpringBootIntegrationTest {

    @InjectWireMock("issuer-service")
    private WireMockServer mockIssuerService;

    protected String expectedAuthorizedCorrelationId;

    @BeforeEach
    void setUp() {
        // setup: the issuer service mock will return an authorized response
        expectedAuthorizedCorrelationId = UUID.randomUUID().toString();
        mockIssuerService.stubFor(
                WireMock.post(urlEqualTo("/authorization"))
                        .withHeader("X-Correlation-ID", WireMock.equalTo(expectedAuthorizedCorrelationId))
                        .willReturn(
                                aResponse()
                                        .withStatus(201)
                                        .withTransformers("response-template")
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(STR."""
                                                {
                                                  "authorizationKrn": "krn:payments:authorization:eu-west-1:{{now format='yyyyMMddHHmmss'}}:transaction:{{randomValue type='UUID'}}?version=1.0.0",
                                                  "status": "AUTHORIZED",
                                                  "statusReason": null
                                                }
                                                """
                                        )
                        )
        );
    }

    @TestConfiguration
    static class ObservationTestConfiguration {
        @Bean
        TestObservationRegistry observationRegistry() {
            return TestObservationRegistry.create();
        }
    }
}
