package com.ead.payments.orders.complete;

import com.ead.payments.orders.Order;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOrderResponse {

    UUID id;
    Long version;
    Order.OrderStatus status;
    Currency currency;
    Long amount;
}
