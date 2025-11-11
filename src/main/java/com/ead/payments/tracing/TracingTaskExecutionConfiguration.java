package com.ead.payments.tracing;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

@Configuration
@RequiredArgsConstructor
class TracingTaskExecutionConfiguration {

    private final TaskDecorator tracingTaskDecorator;

    @Bean
    SimpleAsyncTaskSchedulerCustomizer tracingTaskExecutionCustomizer() {
        return builder -> builder.setTaskDecorator(tracingTaskDecorator);
    }
}

