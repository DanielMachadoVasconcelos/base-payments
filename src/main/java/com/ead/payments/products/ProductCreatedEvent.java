package com.ead.payments.products;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ProductCreatedEvent {

     UUID id;
     String sku;
     String name;
     String description;

     Long weight;
     Long height;
     Long width;
     Long length;

     List<String> tags;
     List<String> categories;
     List<String> details;

     String thumbnailUrl;
     List<String> imagesUrls;
}
