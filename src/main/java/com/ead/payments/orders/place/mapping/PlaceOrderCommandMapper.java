package com.ead.payments.orders.place.mapping;

import com.ead.payments.orders.LineItem;
import com.ead.payments.orders.place.PlaceOrderCommand;
import com.ead.payments.orders.place.request.LineItemRequest;
import com.ead.payments.orders.place.request.PlaceOrderRequestV1;
import com.ead.payments.orders.place.request.PlaceOrderRequestV2;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LineItemMapper.class})
public interface PlaceOrderCommandMapper {
    
    /**
     * Maps V1 request to command. Uses empty list for line items.
     * MapStruct automatically generates the implementation.
     * No validation - validation happens in OrderAggregate constructor.
     */
    @Mapping(target = "id", source = "orderId")
    @Mapping(target = "currency", source = "request.currency")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "lineItems", expression = "java(java.util.List.of())")
    PlaceOrderCommand toCommand(PlaceOrderRequestV1 request, UUID orderId);
    
    /**
     * Maps V2 request to command. 
     * MapStruct automatically uses LineItemMapper (from 'uses') to convert lineItems.
     * Amount is computed from line items using an expression.
     */
    @Mapping(target = "id", source = "orderId")
    @Mapping(target = "currency", source = "request.currency")
    @Mapping(target = "amount", expression = "java(computeTotalFromLineItems(request.lineItems()))")
    @Mapping(target = "lineItems", source = "request.lineItems")
    PlaceOrderCommand toCommand(PlaceOrderRequestV2 request, UUID orderId);
    
    /**
     * Helper method to compute total from line items.
     * Used in @Mapping expression for V2 mapping.
     */
    default Long computeTotalFromLineItems(List<LineItemRequest> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            return 0L;
        }
        return lineItems.stream()
            .mapToLong(item -> item.unitPrice() * item.quantity())
            .sum();
    }
}
