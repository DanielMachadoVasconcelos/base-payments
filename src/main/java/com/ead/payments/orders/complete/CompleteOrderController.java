package com.ead.payments.orders.complete;

import com.ead.payments.logging.OrderIdLoggingContext;
import com.ead.payments.orders.OrderNotFoundException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/orders")
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class CompleteOrderController {

    CompleteOrderService completeOrderService;

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/{order_id}/complete", headers = "version=1.0.0")
    public CompleteOrderResponse completeOrder(@PathVariable("order_id") @NotNull UUID orderId) {
        try (OrderIdLoggingContext.Scope ignored = OrderIdLoggingContext.withOrderId(orderId)) {
            var order = completeOrderService.handle(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));

            return new CompleteOrderResponse(
                    order.id(),
                    order.version(),
                    order.status(),
                    order.currency(),
                    order.amount()
            );
        }
    }
}
