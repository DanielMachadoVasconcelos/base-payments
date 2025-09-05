package com.ead.payments.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Overrides the Docker-Compose provided Zipkin connection so spans are sent
 * directly to Tempo's Zipkin-compatible ingest endpoint.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class TempoZipkinConnectionConfiguration {

    private static final String DEFAULT_ENDPOINT = "http://localhost:9412/api/v2/spans";

    @Bean
    @Primary
    ZipkinConnectionDetails tempoZipkinConnectionDetails(Environment environment) {
        String endpoint = firstNonEmpty(
                environment.getProperty("management.tracing.exporter.zipkin.endpoint"),
                environment.getProperty("management.zipkin.tracing.endpoint"),
                environment.getProperty("spring.zipkin.base-url"),
                DEFAULT_ENDPOINT);
        log.info("Tempo ZipkinConnectionDetails overriding Compose connection with endpoint {}", endpoint);
        return () -> endpoint;
    }

    private String firstNonEmpty(String... candidates) {
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return DEFAULT_ENDPOINT;
    }
}
