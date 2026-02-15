package com.ead.payments.orders;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "line_items", schema = "orders")
@Data
@NoArgsConstructor
public class OrderLineItemEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderAggregate order;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price_minor_units", nullable = false)
    private Long unitPrice;
    
    @Column
    private String reference;
    
    public OrderLineItemEntity(OrderAggregate order, LineItem lineItem) {
        this.order = order;
        this.name = lineItem.name();
        this.quantity = lineItem.quantity();
        this.unitPrice = lineItem.unitPrice();
        this.reference = lineItem.reference();
    }
    
    public Long getLineTotal() {
        return unitPrice * quantity;
    }
}

