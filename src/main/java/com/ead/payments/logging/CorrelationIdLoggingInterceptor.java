package com.ead.payments.logging;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdLoggingInterceptor extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String TRACE_ID_KEY = "traceId";
    private static final String INVALID_TRACE_ID = "00000000000000000000000000000000";

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {

        try {
            // Extract Correlation ID from the request header (or generate one if missing)
            String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER))
                    .orElse(UUID.randomUUID().toString());

            // Add Correlation ID to the MDC
            MDC.put(CORRELATION_ID_KEY, correlationId);

            // Continue the filter chain with the correlation ID
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            filterChain.doFilter(request, response);
            currentTraceId().ifPresent(traceId -> response.setHeader(TRACE_ID_HEADER, traceId));

        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    private Optional<String> currentTraceId() {
        String traceId = Optional.ofNullable(MDC.get(TRACE_ID_KEY))
                .orElse(Span.current().getSpanContext().getTraceId());
        if (traceId == null || traceId.isBlank() || INVALID_TRACE_ID.equals(traceId)) {
            return Optional.empty();
        }
        return Optional.of(traceId);
    }
}
