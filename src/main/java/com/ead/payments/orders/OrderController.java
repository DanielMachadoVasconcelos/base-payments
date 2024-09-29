package com.ead.payments.orders;

import com.ead.payments.CommandDispatcher;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Role;
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
@RequestMapping(path = "/v1/orders")
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class OrderController {

    CommandDispatcher commandDispatcher;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceOrderResponse placeOrder(@RequestBody @Valid @NotNull PlaceOrderRequest request) {
        var orderId = UUID.randomUUID();

        commandDispatcher.send(new PlaceOrderCommand(
                orderId,
                request.getCurrency(),
                request.getAmount()
        ));

        return new PlaceOrderResponse(
            orderId,
            request.getCurrency(),
            request.getAmount()
        );
    }
}
