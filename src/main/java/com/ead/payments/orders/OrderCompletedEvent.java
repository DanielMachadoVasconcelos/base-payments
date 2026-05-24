package com.ead.payments.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.springframework.modulith.events.Externalized;

@Value
@ToString
@Externalized("orders-events.v1.topic::#{getId().toString()}")
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class OrderCompletedEvent {

    UUID id;
    Long version;
    Order.OrderStatus status;
    Currency currency;
    Long amount;
}
