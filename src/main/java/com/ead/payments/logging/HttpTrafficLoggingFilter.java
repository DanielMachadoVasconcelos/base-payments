package com.ead.payments.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

@Slf4j
@Component
@RequiredArgsConstructor
// Full request/response payload logging is intentionally restricted to local runs.
@Profile("local")
public class HttpTrafficLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_PAYLOAD_LENGTH = 1024 * 1024;
    private static final Pattern ORDER_ID_PATH_PATTERN =
            Pattern.compile("^/orders/(?<orderId>[0-9a-fA-F\\-]{36})(?:/.*)?$");

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        ContentCachingRequestWrapper requestWrapper = wrapRequest(request);
        ContentCachingResponseWrapper responseWrapper = wrapResponse(response);
        Instant startedAt = Instant.now();

        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(extractOrderIdFromPath(requestWrapper))) {
            filterChain.doFilter(requestWrapper, responseWrapper);
            attachOrderIdForResponseLogging(requestWrapper, responseWrapper);
            logRequest(requestWrapper, startedAt);
            logResponse(requestWrapper, responseWrapper, startedAt);
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    private void attachOrderIdForResponseLogging(ContentCachingRequestWrapper request,
                                                 ContentCachingResponseWrapper response) {
        if (StringUtils.hasText(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY))) {
            return;
        }

        String orderIdFromPathVariables = extractOrderIdFromPathVariables(request);
        if (StringUtils.hasText(orderIdFromPathVariables)) {
            OrderIdLoggingContext.putOrderId(orderIdFromPathVariables);
            return;
        }

        // POST /orders generates the order id during request handling, so recover it
        // from the JSON response before writing the final response log entry.
        extractOrderIdFromResponseBody(response).ifPresent(OrderIdLoggingContext::putOrderId);
    }

    private void logRequest(ContentCachingRequestWrapper request, Instant startedAt) {
        log.info(
                "http.request method={} path={} query={} path_pattern={} path_variables={} headers={} body={} started_at={} order_id={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                Optional.ofNullable(request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).orElse("n/a"),
                pathVariables(request),
                headers(request),
                payload(request.getContentType(), request.getCharacterEncoding(), request.getContentAsByteArray()),
                startedAt,
                MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)
        );
    }

    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             Instant startedAt) {
        log.info(
                "http.response method={} path={} status={} duration_ms={} headers={} body={} order_id={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                Duration.between(startedAt, Instant.now()).toMillis(),
                headers(response),
                payload(response.getContentType(), response.getCharacterEncoding(), response.getContentAsByteArray()),
                MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)
        );
    }

    private Map<String, String> pathVariables(HttpServletRequest request) {
        Object pathVariables = request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map<?, ?> values) {
            return values.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> String.valueOf(entry.getKey()),
                            entry -> String.valueOf(entry.getValue()),
                            (left, right) -> right,
                            LinkedHashMap::new
                    ));
        }
        return Collections.emptyMap();
    }

    private Map<String, List<String>> headers(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        headerName -> Collections.list(request.getHeaders(headerName)),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    private Map<String, List<String>> headers(HttpServletResponse response) {
        return response.getHeaderNames().stream()
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        headerName -> List.copyOf(response.getHeaders(headerName)),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    private String payload(String contentType, String encoding, byte[] body) {
        if (body == null || body.length == 0) {
            return "";
        }
        if (!isTextPayload(contentType)) {
            return "[binary " + body.length + " bytes]";
        }
        Charset charset = StringUtils.hasText(encoding) ? Charset.forName(encoding) : StandardCharsets.UTF_8;
        return new String(body, charset);
    }

    private boolean isTextPayload(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return true;
        }

        MediaType mediaType = MediaType.parseMediaType(contentType);
        return MediaType.APPLICATION_JSON.includes(mediaType)
                || MediaType.APPLICATION_XML.includes(mediaType)
                || MediaType.TEXT_PLAIN.includes(mediaType)
                || MediaType.TEXT_XML.includes(mediaType)
                || MediaType.TEXT_HTML.includes(mediaType)
                || "application".equalsIgnoreCase(mediaType.getType())
                && mediaType.getSubtype().toLowerCase().contains("x-www-form-urlencoded");
    }

    private String extractOrderIdFromPath(HttpServletRequest request) {
        Matcher matcher = ORDER_ID_PATH_PATTERN.matcher(request.getRequestURI());
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group("orderId");
    }

    private String extractOrderIdFromPathVariables(HttpServletRequest request) {
        return Optional.ofNullable(pathVariables(request).get("order_id"))
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private Optional<String> extractOrderIdFromResponseBody(ContentCachingResponseWrapper response) {
        try {
            String payload = payload(response.getContentType(), response.getCharacterEncoding(), response.getContentAsByteArray());
            if (!StringUtils.hasText(payload) || !payload.startsWith("{")) {
                return Optional.empty();
            }
            JsonNode json = objectMapper.readTree(payload);
            return Optional.ofNullable(json.path("id").textValue()).filter(StringUtils::hasText);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            return wrapper;
        }
        return new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            return wrapper;
        }
        return new ContentCachingResponseWrapper(response);
    }
}
