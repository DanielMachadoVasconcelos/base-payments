package com.ead.payments.products;

import java.util.List;
import java.util.UUID;

public record Product(
        UUID id,
        long version,
        String sku,
        String name,
        String description,

        Long weight,
        Long height,
        Long width,
        Long length,

        List<String> tags,
        List<String> categories,
        List<String> details,

        String thumbnailUrl,
        List<String> imagesUrls
) {
}