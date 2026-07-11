package com.ead.payments.orders.place;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "issuer", types = IssuerClient.class)
public class IssuerClientConfiguration {

    @Bean
    RestClientHttpServiceGroupConfigurer issuerHttpServiceGroupConfigurer() {
        return groups -> groups.filterByName("issuer")
                .forEachClient((group, clientBuilder) -> clientBuilder.defaultRequest(request -> {
                    addHeaderWhenPresent(request, "X-Correlation-ID", MDC.get("correlationId"));
                    addHeaderWhenPresent(request, "X-Trace-ID", MDC.get("traceId"));
                    addHeaderWhenPresent(request, "X-Mocked-Issuer", MDC.get("x-mocked-issuer"));
                }));
    }

    private static void addHeaderWhenPresent(RestClient.RequestHeadersSpec<?> request,
                                             String name,
                                             String value) {
        if (StringUtils.hasText(value)) {
            request.header(name, value);
        }
    }
}
