package com.ead.payments.orders;

import java.util.UUID;

public record OrderPlacedResponse(UUID orderId, Long version, String currency, Long amount) {
}
