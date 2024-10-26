package com.ead.payments.products;

import com.ead.payments.SpringBootIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateProductsControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "marketing", roles = "ADMIN")
    @DisplayName("Should return 201 when create product")
    void shouldReturn201WhenCreateProduct() throws Exception {
        // given: a valid create product request
        var request = new CreateProductRequest(
                "sku",
                "name",
                "description",
                100L,
                100L,
                100L,
                100L,
                List.of("decor", "80's decor"),
                List.of("home", "decoration"),
                List.of("sofa", "couch"),
                "https://localhost:8080/thumbnail",
                List.of("https//localhost:8080/upper", "https//localhost:8080/lower")
        );

        // when: the create product request is made
        var response = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .content(objectMapper.writeValueAsString(request))
        );

        // then: the response is 201
        response.andDo(print())
                .andExpect(status().isCreated());
    }
}
