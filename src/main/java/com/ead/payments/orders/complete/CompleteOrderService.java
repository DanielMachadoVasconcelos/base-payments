package com.ead.payments.orders.complete;

import static com.ead.payments.orders.Order.OrderStatus.COMPLETED;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.OrderAggregate;
import com.ead.payments.orders.OrderAggregateMapper;
import com.ead.payments.orders.OrderRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompleteOrderService {

    final OrderAggregateMapper orderAggregateMapper;
    final OrderRepository orderRepository;

    public Optional<Order> handle(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(this::complete)
                .map(orderAggregateMapper::toOrder);
    }

    private OrderAggregate complete(OrderAggregate aggregate) {
        if (aggregate.getStatus() == COMPLETED) {
            return aggregate;
        }

        return orderRepository.save(aggregate.complete());
    }
}
