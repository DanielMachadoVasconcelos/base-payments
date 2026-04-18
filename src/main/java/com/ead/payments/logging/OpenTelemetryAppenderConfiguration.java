package com.ead.payments.logging;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenTelemetryAppenderConfiguration implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    OpenTelemetryAppenderConfiguration(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        // Spring Boot configures the SDK; the Logback appender only needs the shared instance.
        OpenTelemetryAppender.install(this.openTelemetry);
    }
}
