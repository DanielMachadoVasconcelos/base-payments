package com.ead.payments.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;

class HttpTrafficLoggingFilterTest {

    private final HttpTrafficLoggingFilter filter = new HttpTrafficLoggingFilter(new ObjectMapper());

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should log the order id from the order path when the request targets a specific order")
    void shouldLogOrderIdFromOrderPathWhenRequestTargetsSpecificOrder() throws Exception {
        String orderId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders/" + orderId);
        request.setQueryString("include=items");
        request.addHeader("X-Correlation-Id", "corr-123");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"status\":\"CREATED\"}".getBytes(StandardCharsets.UTF_8));
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/orders/{order_id}");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("order_id", orderId));

        MockHttpServletResponse response = new MockHttpServletResponse();
        ListAppender<ILoggingEvent> appender = attachAppender();

        FilterChain chain = (servletRequest, servletResponse) -> {
            servletRequest.getInputStream().readAllBytes();
            HttpServletResponse currentResponse = (HttpServletResponse) servletResponse;
            currentResponse.setStatus(200);
            currentResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            currentResponse.addHeader("X-Result", "ok");
            currentResponse.getWriter().write("{\"id\":\"" + orderId + "\",\"status\":\"APPROVED\"}");
        };

        this.filter.doFilter(request, response, chain);

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(2);
        assertThat(events.getFirst().getFormattedMessage())
                .contains("http.request")
                .contains("path=/orders/" + orderId)
                .contains("query=include=items")
                .contains("path_pattern=/orders/{order_id}")
                .contains("path_variables={order_id=" + orderId + "}")
                .contains("body={\"status\":\"CREATED\"}")
                .contains("order_id=" + orderId);
        assertThat(events.getLast().getFormattedMessage())
                .contains("http.response")
                .contains("status=200")
                .contains("body={\"id\":\"" + orderId + "\",\"status\":\"APPROVED\"}")
                .contains("order_id=" + orderId);
        assertThat(events).allSatisfy(event ->
                assertThat(event.getMDCPropertyMap()).containsEntry(OrderIdLoggingContext.ORDER_ID_KEY, orderId));
        assertThat(response.getContentAsString()).isEqualTo("{\"id\":\"" + orderId + "\",\"status\":\"APPROVED\"}");
        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should log the generated order id when the create order response contains it")
    void shouldLogGeneratedOrderIdWhenCreateOrderResponseContainsIt() throws Exception {
        String orderId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"product\":\"book\",\"quantity\":2}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        ListAppender<ILoggingEvent> appender = attachAppender();

        FilterChain chain = (servletRequest, servletResponse) -> {
            servletRequest.getInputStream().readAllBytes();
            HttpServletResponse currentResponse = (HttpServletResponse) servletResponse;
            currentResponse.setStatus(201);
            currentResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            currentResponse.getWriter().write("{\"id\":\"" + orderId + "\",\"status\":\"CREATED\"}");
        };

        this.filter.doFilter(request, response, chain);

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(2);
        assertThat(events.getFirst().getFormattedMessage())
                .contains("http.request")
                .contains("path=/orders")
                .contains("body={\"product\":\"book\",\"quantity\":2}")
                .contains("order_id=" + orderId);
        assertThat(events.getLast().getFormattedMessage())
                .contains("http.response")
                .contains("status=201")
                .contains("order_id=" + orderId);
        assertThat(events).allSatisfy(event ->
                assertThat(event.getMDCPropertyMap()).containsEntry(OrderIdLoggingContext.ORDER_ID_KEY, orderId));
        assertThat(response.getContentAsString()).isEqualTo("{\"id\":\"" + orderId + "\",\"status\":\"CREATED\"}");
        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should log a binary placeholder when the response payload is not textual")
    void shouldLogBinaryPlaceholderWhenResponsePayloadIsNotTextual() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reports/export");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ListAppender<ILoggingEvent> appender = attachAppender();

        FilterChain chain = (servletRequest, servletResponse) -> {
            HttpServletResponse currentResponse = (HttpServletResponse) servletResponse;
            currentResponse.setStatus(200);
            currentResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            currentResponse.getOutputStream().write(new byte[]{1, 2, 3});
        };

        this.filter.doFilter(request, response, chain);

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(2);
        assertThat(events.getFirst().getFormattedMessage()).contains("body=");
        assertThat(events.getLast().getFormattedMessage())
                .contains("http.response")
                .contains("body=[binary 3 bytes]")
                .contains("order_id=null");
        assertThat(events.getLast().getMDCPropertyMap()).doesNotContainKey(OrderIdLoggingContext.ORDER_ID_KEY);
        assertThat(response.getContentAsByteArray()).containsExactly(1, 2, 3);
        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isNull();
    }

    private ListAppender<ILoggingEvent> attachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(HttpTrafficLoggingFilter.class);
        logger.detachAndStopAllAppenders();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }
}
