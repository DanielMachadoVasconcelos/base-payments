package com.ead.payments.orders.search;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/orders")
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class SearchOrderController {

    SearchOrderService searchOrderService;

    @GetMapping(path = "/{order_id}", headers = "version=1.0.0")
    @ResponseStatus(HttpStatus.OK)
    public Optional<SearchOrderResponse> searchOrder(@PathVariable("order_id") @NotNull UUID orderId) {
        return searchOrderService.search(orderId)
                .map(order -> new SearchOrderResponse(
                        order.id(),
                        order.version(),
                        order.currency(),
                        order.amount()
                ));
    }
}
