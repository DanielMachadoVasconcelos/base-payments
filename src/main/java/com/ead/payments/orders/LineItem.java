package com.ead.payments.orders;

import com.google.common.base.Preconditions;

public record LineItem(
        String name,
        String reference,
        Long quantity,
        Long unitPrice,
        Long amount,
        Long totalAmount
) {

    public LineItem {
        Preconditions.checkNotNull(name, "The name is required");
        Preconditions.checkArgument(!name.isBlank(), "The name is required");
        Preconditions.checkArgument(name.length() <= 255, "The name must be less than or equal to 255 characters");

        Preconditions.checkNotNull(reference, "The reference is required");
        Preconditions.checkArgument(!reference.isBlank(), "The reference is required");
        Preconditions.checkArgument(reference.length() <= 255, "The reference must be less than or equal to 255 characters");

        Preconditions.checkNotNull(quantity, "The quantity is required");
        Preconditions.checkArgument(quantity > 0, "The quantity must be greater than 0");

        Preconditions.checkNotNull(unitPrice, "The unit price is required");
        Preconditions.checkArgument(unitPrice > 0, "The unit price must be greater than 0");

        Preconditions.checkNotNull(amount, "The amount is required");
        Preconditions.checkArgument(amount > 0, "The amount must be greater than 0");

        Preconditions.checkNotNull(totalAmount, "The total amount is required");
        Preconditions.checkArgument(totalAmount > 0, "The total amount must be greater than 0");
        Preconditions.checkArgument(totalAmount == amount * quantity, "The total amount must be equal to the amount multiplied by the quantity");
    }
}
