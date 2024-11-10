package com.ead.payments.products;

import com.ead.payments.eventsourcing.BaseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.modulith.events.Externalized;

@Data
@ToString
@NoArgsConstructor
@Externalized("products-events.v1.topic::#{getId().toString()}")
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ProductCreatedEvent extends BaseEvent {

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
    private  List<String> imagesUrls;
}
