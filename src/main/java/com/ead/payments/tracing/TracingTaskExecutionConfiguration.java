package com.ead.payments.tracing;

import org.springframework.boot.task.SimpleAsyncTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@Configuration
class TracingTaskExecutionConfiguration {

    @Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    @Bean
    SimpleAsyncTaskSchedulerCustomizer tracingTaskExecutionCustomizer(
            ContextPropagatingTaskDecorator contextPropagatingTaskDecorator) {
        // Spring Boot 4 recommends the shared context-propagation decorator instead of
        // manually copying Brave trace state and MDC entries.
        return builder -> builder.setTaskDecorator(contextPropagatingTaskDecorator);
    }
}
