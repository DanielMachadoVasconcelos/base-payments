package com.ead.payments.orders;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
    public OrderPlacedResponse placeOrder(@RequestBody @Valid PlaceOrderRequest request)  {
        Order order = new Order(request.id(), request.version(), request.currency(), request.amount());
        var orderPlaced = ordersService.placeOrder(order);
        return new OrderPlacedResponse(orderPlaced.id(), orderPlaced.version(), orderPlaced.currency(), orderPlaced.amount());
    }
}

