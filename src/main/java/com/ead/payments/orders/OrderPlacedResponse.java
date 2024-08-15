package com.ead.payments.orders;

import java.util.Set;
import java.util.UUID;

public record OrderPlacedResponse(UUID orderId, Long version, String currency, Long amount, Set<LineItem> lineItems) {
}
