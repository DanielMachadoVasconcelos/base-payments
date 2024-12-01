package com.ead.payments.products;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/products")
@RolesAllowed({"ROLE_ADMIN"})
public class ProductController {

    ProductService productService;

    @PostMapping(headers = "version=1.0.0")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateProductResponse createProduct(@RequestBody @Valid @NotNull  CreateProductRequest request) {
        var productId = UUID.randomUUID();

        productService.handle(new CreateProductCommand(
                productId,
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getWeight(),
                request.getHeight(),
                request.getWidth(),
                request.getLength(),
                request.getTags(),
                request.getCategories(),
                request.getDetails(),
                request.getThumbnailUrl(),
                request.getImagesUrls()
        ));

        return new CreateProductResponse(
            productId,
            request.getSku(),
            request.getName(),
            request.getDescription(),
            request.getWeight(),
            request.getHeight(),
            request.getWidth(),
            request.getLength(),
            request.getTags(),
            request.getCategories(),
            request.getDetails(),
            request.getThumbnailUrl(),
            request.getImagesUrls()
        );
    }
}
