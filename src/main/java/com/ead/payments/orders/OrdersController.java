package com.ead.payments.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    public OrderPlacedResponse placeOrder(@RequestBody PlaceOrderRequest request) {
        var orderPlaced = ordersService.placeOrder(new Order(request.id(), request.lineItems()));
        return new OrderPlacedResponse(orderPlaced.id());
    }

}

