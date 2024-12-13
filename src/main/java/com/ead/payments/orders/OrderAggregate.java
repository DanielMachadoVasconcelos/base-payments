package com.ead.payments.orders;

import com.ead.payments.orders.place.PlaceOrderCommand;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.security.authorization.method.AuthorizeReturnObject;

@Data
@ToString
@NoArgsConstructor
@AuthorizeReturnObject
@Entity(name = "orders")
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(as = OrderAggregate.class)
@JsonDeserialize(as = OrderAggregate.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderAggregate extends AbstractAggregateRoot<OrderAggregate>  implements  Persistable<UUID> {

    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Long version;

    @Column
    @Enumerated(EnumType.STRING)
    private Order.OrderStatus status;

    @NotNull
    private String currency;

    @Min(0)
    @NotNull
    private Long amount;

    public OrderAggregate(PlaceOrderCommand command) {
        Preconditions.checkNotNull(command.currency(), "The currency is required");

        Preconditions.checkArgument(!command.currency().isBlank(), "The currency is required");
        Preconditions.checkArgument(command.currency().length() == 3, "The currency must be in ISO 4217 format");

        Preconditions.checkNotNull(command.amount(), "The amount is required");
        Preconditions.checkArgument(command.amount() > 0, "The amount must be greater than 0");

        this.id = command.id();
        this.version = 0L;
        this.status = Order.OrderStatus.PLACED;
        this.currency = command.currency();
        this.amount = command.amount();

        registerEvent(new OrderPlacedEvent(
                command.id(),
                version,
                status,
                command.currency(),
                command.amount()
        ));
    }

    public OrderAggregate cancel() {
        Preconditions.checkState(status == Order.OrderStatus.PLACED, "The order must be placed to be cancelled");

        this.status = Order.OrderStatus.CANCELLED;

        registerEvent(new OrderCancelledEvent(
                id,
                version,
                status,
                currency,
                amount
        ));

        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return version == null || version == 0;
    }
}