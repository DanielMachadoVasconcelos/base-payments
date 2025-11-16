package com.ead.payments.orders;

import com.ead.payments.orders.place.PlaceOrderCommand;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.security.authorization.method.AuthorizeReturnObject;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static com.ead.payments.orders.Order.OrderStatus;

@Data
@ToString
@NoArgsConstructor
@AuthorizeReturnObject
@Entity(name = "orders")
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(as = OrderAggregate.class)
@JsonDeserialize(as = OrderAggregate.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderAggregate extends AbstractAggregateRoot<OrderAggregate> implements Persistable<UUID> {

    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Long version;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotNull
    private Currency currency;

    @Min(0)
    @NotNull
    private Long amount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineItemEntity> lineItems = new ArrayList<>();

    public OrderAggregate(PlaceOrderCommand command) {
        // Existing validations
        Preconditions.checkNotNull(command.currency(), "The currency is required");
        Preconditions.checkNotNull(command.lineItems(), "Line items are required");
        // Note: Line items can be empty for V1 orders
        
        // Validate each line item
        for (LineItem item : command.lineItems()) {
            Preconditions.checkArgument(item.quantity() > 0, 
                "Line item quantity must be greater than 0");
            Preconditions.checkArgument(item.unitPrice() != null && item.unitPrice() >= 0, 
                "Line item unit price must be non-negative");
        }
        
        // Convert LineItems to entities
        this.lineItems = command.lineItems().stream()
            .map(item -> new OrderLineItemEntity(this, item))
            .toList();
        
        // Compute total from line items (if any)
        Long computedTotal = lineItems.stream()
            .mapToLong(OrderLineItemEntity::getLineTotal)
            .sum();
        
        // Validate that computed total matches the provided amount (if provided)
        if (command.amount() != null && !command.lineItems().isEmpty()) {
            Preconditions.checkArgument(
                computedTotal.equals(command.amount()),
                "Line items total (%s) does not match provided amount (%s)",
                computedTotal,
                command.amount()
            );
        }
        
        // For V1 orders (empty line items), use the provided amount
        // For V2 orders, use computed total from line items
        this.amount = command.amount() != null ? command.amount() : computedTotal;
        Preconditions.checkArgument(this.amount > 0, "The amount must be greater than 0");
        
        this.id = command.id();
        this.status = OrderStatus.PLACED;
        this.currency = command.currency();
        
        registerEvent(new OrderPlacedEvent(
            command.id(),
            version,
            status,
            command.currency(),
            this.amount,
            command.lineItems()  // Include line items in event
        ));
    }

    public OrderAggregate cancel() {
        Preconditions.checkState(status != OrderStatus.COMPLETED, "The order is already completed");

        this.status = OrderStatus.CANCELLED;

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
