package com.ead.payments.orders.place;

import com.ead.payments.orders.LineItem;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

public record PlaceOrderCommand(
        UUID id,
        Currency currency,
        Long amount,  // Keep for backward compatibility during transition
        List<LineItem> lineItems  // NEW
) {
}
