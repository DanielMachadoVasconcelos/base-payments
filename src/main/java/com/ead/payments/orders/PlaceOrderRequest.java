package com.ead.payments.orders;

import java.util.Set;

public record PlaceOrderRequest(Integer id, Set<LineItem> lineItems) {
}
