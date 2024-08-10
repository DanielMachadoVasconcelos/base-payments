package com.ead.payments.orders;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
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
                Optional.ofNullable(source.id()).orElseGet(UUID::randomUUID),
                Optional.ofNullable(source.version()).orElse(1L),
                new OrderPayload(source.currency(), source.amount()),
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
                source.payload().amount()
        );
   }
}
