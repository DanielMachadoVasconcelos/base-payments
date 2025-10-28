package com.ead.payments.tracing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinConnectionDetails;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(ZipkinConnectionDetails.class)
class TracingDiagnosticsConfiguration {

    private final ZipkinConnectionDetails zipkinConnectionDetails;

    @Bean
    ApplicationRunner tracingDiagnostics() {
        return args -> {
            String endpoint = zipkinConnectionDetails.getSpanEndpoint();
            if (StringUtils.hasText(endpoint)) {
                log.info("Zipkin span endpoint configured as {}", endpoint);
            } else {
                log.warn("Zipkin span endpoint is not configured");
            }
        };
    }
}
