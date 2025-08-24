package com.ead.payments.logging;

import com.google.common.base.Preconditions;

import java.util.UUID;

public record CorrelationId(String value) {

    public CorrelationId {
        Preconditions.checkNotNull(value, "CorrelationId value can't be null");
        Preconditions.checkArgument(!value.isBlank(), "CorrelationId value can't be blank");
        Preconditions.checkArgument(value.length() == 36, "CorrelationId value must be 36 characters long");

        // check if the string is a UUID
        Preconditions.checkArgument(
                value.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"),
                "CorrelationId value is not in the correct format: " + value);

    }

    public static CorrelationId random() {
        return new CorrelationId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
