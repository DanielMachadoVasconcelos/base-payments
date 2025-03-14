package com.ead.payments.orders.place;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class IssuerClientConfiguration {

    // create a bean for the client request factory
    @Bean
    public ClientHttpRequestFactory requestFactory() {
        return new SimpleClientHttpRequestFactory();
    }

    @Bean
    public RestClient defaultClient(ClientHttpRequestFactory requestFactory) {
        return RestClient.builder()
                .defaultRequest(request ->
                     request.header("X-Correlation-ID", MDC.get("correlationId"))
                            .header("X-Trace-ID", MDC.get("traceId"))
                            .header("X-Mocked-Issuer", MDC.get("x-mocked-issuer"))
                )
                .baseUrl("http://localhost:8081")
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClientAdapter defaultRestClientAdapter(RestClient defaultClient) {
        return RestClientAdapter.create(defaultClient);
    }

    @Bean
    public HttpServiceProxyFactory defaultHttpServiceProxyFactory(RestClientAdapter defaultRestClientAdapter) {
        return HttpServiceProxyFactory.builderFor(defaultRestClientAdapter)
                .build();
    }

    @Bean
    public IssuerClient issuerClient(HttpServiceProxyFactory defaultHttpServiceProxyFactory) {
        return defaultHttpServiceProxyFactory.createClient(IssuerClient.class);
    }
}
