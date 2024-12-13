package com.ead.payments.orders.cancel;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.OrderAggregate;
import com.ead.payments.orders.OrderRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CancelOrderService {

    OrderRepository orderRepository;

    public Optional<Order> handle(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderAggregate::cancel)
                .map(orderRepository::save)
                .map(aggregate -> new Order(
                        aggregate.getId(),
                        aggregate.getVersion(),
                        aggregate.getStatus(),
                        aggregate.getCurrency(),
                        aggregate.getAmount()
                ));
    }
}
