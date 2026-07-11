package com.ead.payments.orders.place;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface IssuerClient {

    @PostExchange(
            url = "/authorization",
            contentType = MediaType.APPLICATION_JSON_VALUE,
            accept = MediaType.APPLICATION_JSON_VALUE
    )
    IssuerAuthorizationResponse authorize(@RequestBody IssuerAuthorizationRequest request);
}
