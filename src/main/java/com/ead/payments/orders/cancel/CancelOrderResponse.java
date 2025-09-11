package com.ead.payments.orders.cancel;

import com.ead.payments.orders.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResponse {

    UUID id;
    Long version;
    Order.OrderStatus status;
    Currency currency;
    Long amount;
}
