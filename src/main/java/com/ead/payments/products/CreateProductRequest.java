package com.ead.payments.products;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

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
