package com.ead.payments.orders.cancel;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.OrderAggregate;
import com.ead.payments.orders.OrderAggregateMapper;
import com.ead.payments.orders.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelOrderService {

    final OrderAggregateMapper orderAggregateMapper;
    final OrderRepository orderRepository;

    public Optional<Order> handle(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderAggregate::cancel)
                .map(orderRepository::save)
                .map(orderAggregateMapper::toOrder);
    }
}
