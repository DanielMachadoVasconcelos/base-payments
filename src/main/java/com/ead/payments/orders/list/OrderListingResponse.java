package com.ead.payments.orders.list;

import java.util.List;

public record OrderListingResponse(
        List<OrderListingItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
