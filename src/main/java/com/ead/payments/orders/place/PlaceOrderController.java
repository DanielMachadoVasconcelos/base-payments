package com.ead.payments.orders.place;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/orders")
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class PlaceOrderController {

    PlaceOrderService placeOrderService;

    @PostMapping(headers = "version=1.0.0")
    @ResponseStatus(HttpStatus.CREATED)
    @Observed(name = "http.orders.create", contextualName = "POST /orders")
    public PlaceOrderResponse placeOrder(@RequestBody @Valid @NotNull PlaceOrderRequest request) {

        UUID orderId = UUID.randomUUID();

        // TODO: make sure to allow the client of this API to place an order with its own ID
        var order = placeOrderService.handle(new PlaceOrderCommand(
                orderId,
                request.currency(),
                request.amount()
        ));

        return new PlaceOrderResponse(
            order.id(),
            order.currency(),
            order.amount()
        );
    }
}
