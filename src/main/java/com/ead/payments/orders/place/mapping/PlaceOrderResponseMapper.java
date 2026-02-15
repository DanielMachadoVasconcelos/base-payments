package com.ead.payments.orders.place.mapping;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.mapping.LineItemResponseMapper;
import com.ead.payments.orders.place.response.PlaceOrderResponseV1;
import com.ead.payments.orders.place.response.PlaceOrderResponseV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LineItemResponseMapper.class})
public interface PlaceOrderResponseMapper {
    
    /**
     * Maps Order domain to V1 response (without line items).
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "amount", source = "amount")
    PlaceOrderResponseV1 toResponseV1(Order order);
    
    /**
     * Maps Order domain to V2 response (with line items).
     * MapStruct automatically uses LineItemResponseMapper (from 'uses') to convert lineItems.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "lineItems", source = "lineItems")
    PlaceOrderResponseV2 toResponseV2(Order order);
}
