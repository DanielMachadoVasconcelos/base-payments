package com.ead.payments.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceOrderService {

    final OrderRepository orderRepository;

    public Order handle(PlaceOrderCommand command) {

        // create a new OrderAggregate object with the command object
        OrderAggregate aggregate = new OrderAggregate(command);

        // save the order object in the repository
        aggregate = orderRepository.save(aggregate);

        // return the order object
        return new Order(
            aggregate.getId(),
            aggregate.getVersion(),
            aggregate.getCurrency(),
            aggregate.getAmount()
        );
    }
}
