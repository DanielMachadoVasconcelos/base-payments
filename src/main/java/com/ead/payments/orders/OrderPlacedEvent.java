package com.ead.payments.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.springframework.modulith.events.Externalized;

@Value
@ToString
@Externalized("orders-events.v1.topic::#{getId().toString()}")
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class OrderPlacedEvent {

    UUID id;
    Long version;
    String currency;
    Long amount;

}
