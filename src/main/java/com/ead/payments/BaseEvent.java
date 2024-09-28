package com.ead.payments;

import com.ead.payments.orders.OrderPlacedEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeName(value = "type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,  property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderPlacedEvent.class, name = "order_placed"),
})
public abstract class BaseEvent extends Message {
    private int version;
}
