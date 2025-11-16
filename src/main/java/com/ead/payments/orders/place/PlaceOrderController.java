package com.ead.payments.orders.place;

import com.ead.payments.orders.place.mapping.PlaceOrderCommandMapper;
import com.ead.payments.orders.place.mapping.PlaceOrderResponseMapper;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.ead.payments.orders.place.request.PlaceOrderRequestV2;
import com.ead.payments.orders.place.response.PlaceOrderResponseV1;
import com.ead.payments.orders.place.response.PlaceOrderResponseV2;
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
    PlaceOrderCommandMapper commandMapper;
    PlaceOrderResponseMapper responseMapper;

    /**
     * V1 endpoint: Explicit version header required.
     * Method overloading allows same method name with different parameter types.
     */
    @PostMapping(headers = "version=1.0.0")
    @ResponseStatus(HttpStatus.CREATED)
    @Observed(
        name = "http.orders.create",
        contextualName = "POST /orders",
        lowCardinalityKeyValues = {"version", "1.0.0"}
    )
    public PlaceOrderResponseV1 placeOrder(
            @RequestBody @Valid @NotNull PlaceOrderRequestV1 request) {
        
        UUID orderId = UUID.randomUUID();
        var command = commandMapper.toCommand(request, orderId);
        var order = placeOrderService.handle(command);
        
        return responseMapper.toResponseV1(order);
    }
    
    /**
     * V2 endpoint: Default (no version header required).
     * Spring will route requests without version header to this method.
     * Method overloading with different parameter type (PlaceOrderRequestV2).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Observed(
        name = "http.orders.create",
        contextualName = "POST /orders",
        lowCardinalityKeyValues = {"version", "2.0.0"}
    )
    public PlaceOrderResponseV2 placeOrder(
            @RequestBody @Valid @NotNull PlaceOrderRequestV2 request) {
        
        UUID orderId = UUID.randomUUID();
        // MapStruct automatically uses LineItemMapper (from 'uses' parameter) to convert lineItems
        var command = commandMapper.toCommand(request, orderId);
        var order = placeOrderService.handle(command);
        
        return responseMapper.toResponseV2(order);
    }
}
