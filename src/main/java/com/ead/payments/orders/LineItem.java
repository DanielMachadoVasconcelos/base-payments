package com.ead.payments.orders;

import com.google.common.base.Preconditions;

public record LineItem(
    String name,
    int quantity,
    Long unitPrice,  // in minor units (e.g., cents)
    String reference  // nullable
) {
    public LineItem {
        Preconditions.checkArgument(name != null && !name.isBlank(), 
            "Line item name is required");
        Preconditions.checkArgument(quantity > 0, 
            "Line item quantity must be greater than 0");
        Preconditions.checkArgument(unitPrice != null && unitPrice >= 0, 
            "Line item unit price must be non-negative");
    }
    
    public Long lineTotal() {
        return unitPrice * quantity;
    }
}

