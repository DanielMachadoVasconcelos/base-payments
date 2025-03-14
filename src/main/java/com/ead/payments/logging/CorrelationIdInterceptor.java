package com.ead.payments.logging;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CorrelationIdInterceptor extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String TRACE_ID_KEY = "traceId";

    private final Tracer tracer;

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

            // Add trace ID to MDC
            Span currentSpan = Optional.ofNullable(tracer.currentSpan()).orElseGet(tracer::nextSpan);
            MDC.put(TRACE_ID_KEY, currentSpan.context().traceId());

            // Continue the filter chain with the correlation ID
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            response.setHeader(TRACE_ID_KEY, currentSpan.context().traceId());
            filterChain.doFilter(request, response);

        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
