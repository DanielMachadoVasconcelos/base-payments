package com.ead.payments.logging;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class OrderIdLoggingContextTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should set the order id while the scope is open")
    void shouldSetOrderIdWhenScopeIsOpen() {
        String orderId = UUID.randomUUID().toString();

        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(orderId)) {
            assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isEqualTo(orderId);
        }

        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should set the order id from an UUID while the scope is open")
    void shouldSetOrderIdFromUuidWhenScopeIsOpen() {
        UUID orderId = UUID.randomUUID();

        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(orderId)) {
            assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isEqualTo(orderId.toString());
        }

        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should restore the previous order id when a nested scope closes")
    void shouldRestorePreviousOrderIdWhenNestedScopeCloses() {
        String parentOrderId = UUID.randomUUID().toString();
        String nestedOrderId = UUID.randomUUID().toString();

        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(parentOrderId)) {
            try (OrderIdLoggingContext.Scope nested = OrderIdLoggingContext.withOrderId(nestedOrderId)) {
                assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isEqualTo(nestedOrderId);
            }

            assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isEqualTo(parentOrderId);
        }

        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should ignore blank order ids when a scope is created")
    void shouldIgnoreBlankOrderIdWhenScopeIsCreated() {
        String parentOrderId = UUID.randomUUID().toString();
        MDC.put(OrderIdLoggingContext.ORDER_ID_KEY, parentOrderId);

        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(" ")) {
            assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isEqualTo(parentOrderId);
        }

        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isEqualTo(parentOrderId);
    }

    @Test
    @DisplayName("Should ignore blank order ids when the context is updated directly")
    void shouldIgnoreBlankOrderIdWhenContextIsUpdatedDirectly() {
        String orderId = UUID.randomUUID().toString();
        MDC.put(OrderIdLoggingContext.ORDER_ID_KEY, orderId);

        OrderIdLoggingContext.putOrderId("");

        assertThat(MDC.get(OrderIdLoggingContext.ORDER_ID_KEY)).isEqualTo(orderId);
    }
}
