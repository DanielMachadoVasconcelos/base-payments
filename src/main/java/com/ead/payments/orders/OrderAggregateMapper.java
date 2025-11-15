package com.ead.payments.orders;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderAggregateMapper {
    
    /**
     * Maps OrderAggregate to Order domain record.
     * MapStruct automatically handles simple field mappings.
     */
    @Mapping(target = "lineItems", source = "lineItems")
    Order toOrder(OrderAggregate orderAggregate);
    
    /**
     * Maps OrderLineItemEntity to LineItem value object.
     * MapStruct automatically generates this mapping.
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "reference", source = "reference")
    LineItem toLineItem(OrderLineItemEntity entity);
    
    /**
     * Maps list of OrderLineItemEntity to list of LineItem.
     * MapStruct automatically handles list mapping.
     */
    List<LineItem> toLineItems(List<OrderLineItemEntity> entities);
}
