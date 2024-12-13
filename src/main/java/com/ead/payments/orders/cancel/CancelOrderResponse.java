package com.ead.payments.orders.cancel;

import com.ead.payments.orders.Order;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResponse {

    UUID id;
    Long version;
    Order.OrderStatus status;
    String currency;
    Long amount;
}
