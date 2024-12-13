package com.ead.payments.orders.cancel;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path =  "/orders")
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class CancelOrderController {

    CancelOrderService cancelOrderService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{order_id}/cancel", headers = "version=1.0.0")
    public Optional<CancelOrderResponse> cancelOrder(@PathVariable( "order_id") @NotNull UUID orderId) {
        return cancelOrderService.handle(orderId)
                .map(order -> new CancelOrderResponse(
                        order.id(),
                        order.version(),
                        order.status(),
                        order.currency(),
                        order.amount()
                ));
    }
}
