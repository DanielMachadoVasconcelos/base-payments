package com.ead.payments.orders.list;

import com.ead.payments.orders.Order.OrderStatus;
import com.ead.payments.orders.response.LineItemResponse;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public record OrderListingItemResponse(
        UUID id,
        Long version,
        Instant createdAt,
        OrderStatus status,
        Currency currency,
        Long amount,
        List<LineItemResponse> lineItems
) {
}
