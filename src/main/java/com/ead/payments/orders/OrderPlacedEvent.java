package com.ead.payments.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.modulith.events.Externalized;

@Data
@ToString
@Externalized("orders-events.v1.topic::#{getId().toString()}")
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class OrderPlacedEvent  {

    private final UUID orderId;
    private final String currency;
    private final Long amount;

}
