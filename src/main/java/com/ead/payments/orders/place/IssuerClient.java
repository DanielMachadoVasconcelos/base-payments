package com.ead.payments.orders.place;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface IssuerClient {

    @PostExchange("/authorization")
    IssuerAuthorizationResponse authorize(@RequestBody IssuerAuthorizationRequest request);
}
