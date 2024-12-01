package com.ead.payments.products;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    final ProductRepository productRepository;

    public Product handle(CreateProductCommand command) {
        ProductAggregate aggregate = new ProductAggregate(command);
        aggregate = productRepository.save(aggregate);
        return new Product(
                aggregate.getId(),
                aggregate.getVersion(),
                aggregate.getSku(),
                aggregate.getName(),
                aggregate.getDescription(),
                aggregate.getWeight(),
                aggregate.getHeight(),
                aggregate.getWidth(),
                aggregate.getLength(),
                aggregate.getTags(),
                aggregate.getCategories(),
                aggregate.getDetails(),
                aggregate.getThumbnailUrl(),
                aggregate.getImagesUrls()
        );
    }
}
