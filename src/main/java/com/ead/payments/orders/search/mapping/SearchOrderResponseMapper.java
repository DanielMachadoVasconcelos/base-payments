package com.ead.payments.orders.search.mapping;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.mapping.LineItemResponseMapper;
import com.ead.payments.orders.search.SearchOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LineItemResponseMapper.class})
public interface SearchOrderResponseMapper {
    
    /**
     * Maps Order domain to SearchOrderResponse.
     * MapStruct automatically uses LineItemResponseMapper (from 'uses') to convert lineItems.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "lineItems", source = "lineItems")
    SearchOrderResponse from(Order order);
}
