package com.ead.payments.orders;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.security.authorization.method.AuthorizeReturnObject;

@Data
@Aggregate
@AllArgsConstructor
@NoArgsConstructor
@AuthorizeReturnObject
@JsonSerialize(as = OrderAggregate.class)
@JsonDeserialize(as = OrderAggregate.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderAggregate {

    @AggregateIdentifier
    private UUID orderId;
    private String currency;
    private Long amount;

    @CommandHandler
    public OrderAggregate(PlaceOrderCommand command) {
        Preconditions.checkNotNull(command.getOrderId(), "The order id is required");
        Preconditions.checkNotNull(command.getCurrency(), "The currency is required");
        Preconditions.checkArgument(!command.getCurrency().isBlank(), "The currency is required");
        Preconditions.checkArgument(command.getCurrency().length() == 3, "The currency must be in ISO 4217 format");
        Preconditions.checkNotNull(command.getAmount(), "The amount is required");
        Preconditions.checkArgument(command.getAmount() > 0, "The amount must be greater than 0");

        apply(new OrderPlacedEvent(
                command.getOrderId(),
                command.getCurrency(),
                command.getAmount()
        ));
    }

    @EventSourcingHandler
    public void on(OrderPlacedEvent event) {
        this.orderId = event.getOrderId();
        this.currency = event.getCurrency();
        this.amount = event.getAmount();
    }
}