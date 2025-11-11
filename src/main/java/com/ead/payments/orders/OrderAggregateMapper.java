package com.ead.payments.orders;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderAggregateMapper {


    Order toOrder(OrderAggregate orderAggregate);

}
