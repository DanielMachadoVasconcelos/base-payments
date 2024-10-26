package com.ead.payments.orders;

import com.ead.payments.eventsourcing.BaseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.modulith.events.Externalized;

@Data
@ToString
@NoArgsConstructor
@Externalized("orders-events.v1.topic::#{getId().toString()}")
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class OrderPlacedEvent extends BaseEvent {

    private UUID id;
    private String currency;
    private Long amount;

}
