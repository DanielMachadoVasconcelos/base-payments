package com.ead.payments.orders;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/orders")
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class OrderController {

    PlaceOrderService placeOrderService;

    @PostMapping(headers = "version=1.0.0")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceOrderResponse placeOrder(@RequestBody @Valid @NotNull PlaceOrderRequest request) {

        UUID orderId = UUID.randomUUID();

        // TODO: make sure to allow the client of this API to place an order with its own ID
        var order = placeOrderService.handle(new PlaceOrderCommand(
                orderId,
                request.getCurrency(),
                request.getAmount()
        ));

        return new PlaceOrderResponse(
            order.id(),
            order.currency(),
            order.amount()
        );
    }
}
