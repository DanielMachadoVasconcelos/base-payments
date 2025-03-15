package com.ead.payments.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@ActiveProfiles({"test", "local"})
public class MockedStubsLoggingInterceptor extends OncePerRequestFilter {

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {

        try {

            // Extract all the headers from the request that starts with "X-Mocked-" and add them to the MDC
            StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(request.getHeaderNames().asIterator(), Spliterator.ORDERED),
                            false
                    )
                    .filter(headerName -> headerName.toLowerCase().startsWith("x-mocked-"))
                    .forEach(headerName -> MDC.put(headerName, request.getHeader(headerName)));

            filterChain.doFilter(request, response);

        } finally {
            // Remove all the headers from the MDC
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                if (headerName.toLowerCase().startsWith("x-mocked-")) {
                    MDC.remove(headerName);
                }
            });
        }
    }
}
