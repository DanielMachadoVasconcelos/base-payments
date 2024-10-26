package com.ead.payments.products;

import com.ead.payments.eventsourcing.AggregateRoot;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authorization.method.AuthorizeReturnObject;

@Data
@NoArgsConstructor
@AuthorizeReturnObject
@JsonSerialize(as = ProductAggregate.class)
@JsonDeserialize(as = ProductAggregate.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAggregate extends AggregateRoot {

    private UUID id;
    private String sku;
    private String name;
    private String description;

    private Long weight;
    private Long height;
    private Long width;
    private Long length;

    private List<String> tags;
    private List<String> categories;
    private List<String> details;

    private String thumbnailUrl;
    private List<String> imagesUrls;

    public ProductAggregate(CreateProductCommand command) {
        Preconditions.checkNotNull(command.getId(), "The id is required");
        Preconditions.checkNotNull(command.getSku(), "The sku is required");
        Preconditions.checkArgument(!command.getSku().isBlank(), "The sku is required");
        Preconditions.checkNotNull(command.getName(), "The name is required");
        Preconditions.checkArgument(!command.getName().isBlank(), "The name is required");
        Preconditions.checkNotNull(command.getDescription(), "The description is required");
        Preconditions.checkArgument(!command.getDescription().isBlank(), "The description is required");
        Preconditions.checkNotNull(command.getWeight(), "The weight is required");
        Preconditions.checkArgument(command.getWeight() > 0, "The weight must be greater than 0");
        Preconditions.checkNotNull(command.getHeight(), "The height is required");
        Preconditions.checkArgument(command.getHeight() > 0, "The height must be greater than 0");
        Preconditions.checkNotNull(command.getWidth(), "The width is required");
        Preconditions.checkArgument(command.getWidth() > 0, "The width must be greater than 0");
        Preconditions.checkNotNull(command.getLength(), "The length is required");
        Preconditions.checkArgument(command.getLength() > 0, "The length must be greater than 0");
        Preconditions.checkNotNull(command.getTags(), "The tags is required");
        Preconditions.checkNotNull(command.getCategories(), "The categories is required");

        raiseEvent(new ProductCreatedEvent(
                command.getId(),
                command.getSku(),
                command.getName(),
                command.getDescription(),
                command.getWeight(),
                command.getHeight(),
                command.getWidth(),
                command.getLength(),
                command.getTags(),
                command.getCategories(),
                command.getDetails(),
                command.getThumbnailUrl(),
                command.getImagesUrls()
        ));
    }

    public void apply(ProductCreatedEvent event) {
        this.id = event.getId();
        this.sku = event.getSku();
        this.name = event.getName();
        this.description = event.getDescription();
        this.weight = event.getWeight();
        this.height = event.getHeight();
        this.width = event.getWidth();
        this.length = event.getLength();
        this.tags = event.getTags();
        this.categories = event.getCategories();
        this.details = event.getDetails();
        this.thumbnailUrl = event.getThumbnailUrl();
        this.imagesUrls = event.getImagesUrls();
    }
}