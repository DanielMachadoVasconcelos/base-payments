package com.ead.payments.orders.place.mapping;

import com.ead.payments.orders.LineItem;
import com.ead.payments.orders.place.request.LineItemRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LineItemMapper {
    
    /**
     * Maps LineItemRequest DTO to domain LineItem.
     * MapStruct automatically generates implementation for this mapping.
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "reference", source = "reference")
    LineItem toLineItem(LineItemRequest dto);
    
    /**
     * Maps list of LineItemRequest DTOs to list of domain LineItems.
     * MapStruct automatically handles list mapping by calling toLineItem for each element.
     */
    List<LineItem> toLineItems(List<LineItemRequest> dtos);
}
