package com.ead.payments.orders.search;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.OrderRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchOrderService {

    final OrderRepository orderRepository;

    public Optional<Order> search(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(aggregate -> new Order(
                        aggregate.getId(),
                        aggregate.getVersion(),
                        aggregate.getCurrency(),
                        aggregate.getAmount()
                ));
    }
}
