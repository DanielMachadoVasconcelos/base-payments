package com.ead.payments.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Version;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.security.authorization.method.AuthorizeReturnObject;

@Data
@NoArgsConstructor
@AuthorizeReturnObject
@Entity(name = "products")
@JsonSerialize(as = ProductAggregate.class)
@JsonDeserialize(as = ProductAggregate.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAggregate extends AbstractAggregateRoot<ProductAggregate> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID id;

    @Version
    private long version;

    private String sku;
    private String name;
    private String description;

    private Long weight;
    private Long height;
    private Long width;
    private Long length;

    private String thumbnailUrl;

    @Column(name = "tag")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    private List<String> tags;

    @Column(name = "category")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_categories", joinColumns = @JoinColumn(name = "product_id"))
    private List<String> categories;

    @Column(name = "detail")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_details", joinColumns = @JoinColumn(name = "product_id"))
    private List<String> details;

    @Column(name = "image_url")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images_urls", joinColumns = @JoinColumn(name = "product_id"))
    private List<String> imagesUrls;

    public ProductAggregate(CreateProductCommand command) {
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

        this.sku = command.getSku();
        this.name = command.getName();
        this.description = command.getDescription();
        this.weight = command.getWeight();
        this.height = command.getHeight();
        this.width = command.getWidth();
        this.length = command.getLength();
        this.tags = command.getTags();
        this.categories = command.getCategories();
        this.details = command.getDetails();
        this.thumbnailUrl = command.getThumbnailUrl();
        this.imagesUrls = command.getImagesUrls();

        registerEvent(new ProductCreatedEvent(
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
}