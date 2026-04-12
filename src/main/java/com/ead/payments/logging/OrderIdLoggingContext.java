package com.ead.payments.logging;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public final class OrderIdLoggingContext {

    public static final String ORDER_ID_KEY = "order_id";

    private OrderIdLoggingContext() {
    }

    public static Scope withOrderId(UUID orderId) {
        return withOrderId(orderId != null ? orderId.toString() : null);
    }

    public static Scope withOrderId(String orderId) {
        String previousOrderId = MDC.get(ORDER_ID_KEY);
        if (StringUtils.hasText(orderId)) {
            // Preserve the previous value so nested order flows can safely override it.
            MDC.put(ORDER_ID_KEY, orderId);
        }
        return () -> restore(previousOrderId);
    }

    public static void putOrderId(String orderId) {
        if (StringUtils.hasText(orderId)) {
            MDC.put(ORDER_ID_KEY, orderId);
        }
    }

    private static void restore(String previousOrderId) {
        if (StringUtils.hasText(previousOrderId)) {
            MDC.put(ORDER_ID_KEY, previousOrderId);
            return;
        }
        MDC.remove(ORDER_ID_KEY);
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
