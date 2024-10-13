package com.ead.payments.orders;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@With
@Data
@AllArgsConstructor
public class PlaceOrderCommand  {

    @TargetAggregateIdentifier
    private final UUID orderId;
    private final String currency;
    private final Long amount;
}
