package com.ead.payments.orders;

import java.util.UUID;

public record Order(
        UUID id,
        Long version,
        String currency,
        Long amount
) {

}
