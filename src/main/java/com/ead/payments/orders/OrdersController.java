package com.ead.payments.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderPlacedResponse placeOrder(@RequestBody PlaceOrderRequest request) throws JsonProcessingException {
        Order order = new Order(request.id(), request.version(), request.currency(), request.amount());
        var orderPlaced = ordersService.placeOrder(order);
        return new OrderPlacedResponse(orderPlaced.id());
    }

}

