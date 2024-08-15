package com.ead.payments.orders;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Observed(name = "orders_controller")
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
    public OrderPlacedResponse placeOrder(@RequestBody @Valid PlaceOrderRequest request) {

        Order order = new Order(
                request.id(),
                request.version(),
                request.currency(),
                request.amount(),
                request.lineItems()
        );

        var orderPlaced = ordersService.placeOrder(order);
        return new OrderPlacedResponse(orderPlaced.id(),
                orderPlaced.version(),
                orderPlaced.currency(),
                orderPlaced.amount(),
                orderPlaced.lineItems()
        );
    }

    @GetMapping("/{orderId}")
    @RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
    public Optional<Order> findById(@PathVariable UUID orderId) {
        return ordersService.findById(orderId);
    }

    @PatchMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
    public Order updateOrder(
            @PathVariable(name = "orderId") @NotNull UUID orderId,
            @RequestBody UpdateOrderRequest request) {
        return ordersService.updateOrder(orderId, request.version(), request.amount());
    }
}

