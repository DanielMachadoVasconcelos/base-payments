package com.ead.payments.orders.mapping;

import com.ead.payments.orders.LineItem;
import com.ead.payments.orders.response.LineItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LineItemResponseMapper {
    
    /**
     * Maps LineItem domain to LineItemResponse DTO.
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "reference", source = "reference")
    LineItemResponse from(LineItem lineItem);
    
    /**
     * Maps list of LineItem domain to list of LineItemResponse DTO.
     */
    List<LineItemResponse> from(List<LineItem> lineItems);
}
