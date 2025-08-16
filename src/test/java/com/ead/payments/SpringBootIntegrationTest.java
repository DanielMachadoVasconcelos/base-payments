package com.ead.payments;

import com.ead.payments.mocks.WireMockProvider;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.micrometer.observation.tck.TestObservationRegistry;
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

import static com.ead.payments.mocks.WireMockProvider.IssuerServiceMockProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Execution(ExecutionMode.CONCURRENT)
@EnableWireMock({@ConfigureWireMock(name = "issuer-service", baseUrlProperties = "issuer.client.base-url")})
public class SpringBootIntegrationTest {

    @InjectWireMock("issuer-service")
    private WireMockServer mockIssuerService;

    protected IssuerServiceMockProvider issuerService() {
        return (IssuerServiceMockProvider) WireMockProvider.of(mockIssuerService);
    }

    @TestConfiguration
    static class ObservationTestConfiguration {
        @Bean
        TestObservationRegistry observationRegistry() {
            return TestObservationRegistry.create();
        }
    }
}
