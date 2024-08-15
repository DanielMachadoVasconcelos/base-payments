package com.ead.payments.orders;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface OrderEntityMapper {

    default OrderEntity from(Order source){
        return new OrderEntity(
                source.id(),
                source.version(),
                new OrderPayload(source.currency(), source.amount(), source.lineItems()),
                null,
                null,
                null,
                null
        );
    }

   default Order from(OrderEntity source){
        return new Order(
                source.id(),
                source.version(),
                source.payload().currency(),
                source.payload().amount(),
                source.payload().lineItems()
        );
   }
}
