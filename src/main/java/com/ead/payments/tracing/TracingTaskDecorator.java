package com.ead.payments.tracing;

import brave.Tracer;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

/**
 * Ensures correlation and trace identifiers follow work scheduled on different threads.
 * Useful for {@code @Async} methods, scheduled tasks and virtual threads created by Spring.
 */
@Component
@RequiredArgsConstructor
class TracingTaskDecorator implements TaskDecorator {

    private final Tracer tracer;
    private final CurrentTraceContext currentTraceContext;

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> capturedMdc = MDC.getCopyOfContextMap();
        TraceContext spanContext = currentSpanContext();

        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (capturedMdc != null) {
                MDC.setContextMap(capturedMdc);
            } else {
                MDC.clear();
            }

            try (CurrentTraceContext.Scope scope =
                         spanContext != null ? currentTraceContext.maybeScope(spanContext) : CurrentTraceContext.Scope.NOOP) {
                runnable.run();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    private TraceContext currentSpanContext() {
        return tracer.currentSpan() != null ? tracer.currentSpan().context() : null;
    }
}
