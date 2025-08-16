package com.ead.payments.orders.place;

import com.ead.payments.SpringBootIntegrationTest;
import com.ead.payments.logging.CorrelationId;
import com.ead.payments.mocks.TestMocks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlaceOrderUnauthorizedControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should not allow to place the order when the issuer do not authorize the payment")
    void shouldNotAllowToPlaceTheOrderWhenTheIssuerDoNotAuthorizeThePayment() throws Exception {
        //setup: issuer service with an authorized response
        CorrelationId expectedCorrelationId = CorrelationId.random();
        TestMocks.setup(issuerService())
                 .toRejectTheAuthorizationWith(expectedCorrelationId);

        // given: a valid place order request
        var request = new PlaceOrderRequest("USD", 100L);

        // when: the place order request is made
        var response = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("version", "1.0.0")
                .header("X-Correlation-ID", expectedCorrelationId)
                .content(objectMapper.writeValueAsString(request)));

        // then: the response is 401
        response.andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
