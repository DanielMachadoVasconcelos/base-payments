package com.ead.payments.orders;

import com.ead.payments.eventsourcing.AggregateRoot;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authorization.method.AuthorizeReturnObject;

@Data
@NoArgsConstructor
@AuthorizeReturnObject
@JsonSerialize(as = OrderAggregate.class)
@JsonDeserialize(as = OrderAggregate.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderAggregate extends AggregateRoot {

    private String currency;
    private Long amount;

    public OrderAggregate(PlaceOrderCommand command) {
        Preconditions.checkNotNull(command.getId(), "The id is required");
        Preconditions.checkNotNull(command.getCurrency(), "The currency is required");
        Preconditions.checkArgument(!command.getCurrency().isBlank(), "The currency is required");
        Preconditions.checkArgument(command.getCurrency().length() == 3, "The currency must be in ISO 4217 format");
        Preconditions.checkNotNull(command.getAmount(), "The amount is required");
        Preconditions.checkArgument(command.getAmount() > 0, "The amount must be greater than 0");

        raiseEvent(new OrderPlacedEvent(
                command.getId(),
                command.getCurrency(),
                command.getAmount()
        ));
    }

    public void apply(OrderPlacedEvent event) {
        this.id = event.getId();
        this.currency = event.getCurrency();
        this.amount = event.getAmount();
    }
}