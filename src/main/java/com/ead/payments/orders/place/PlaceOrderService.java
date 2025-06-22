package com.ead.payments.orders.place;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.OrderAggregate;
import com.ead.payments.orders.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ead.payments.orders.place.IssuerService.Authorization;
import static com.ead.payments.orders.place.IssuerService.RejectedAuthorization;

@Service
@RequiredArgsConstructor
public class PlaceOrderService {

    final OrderRepository orderRepository;
    final IssuerService issuerService;

    public Order handle(PlaceOrderCommand command) {

        // authorize the order
        Authorization authorization = issuerService.authorize(command);

        // if the authorization is rejected, throw an exception
        if (authorization instanceof RejectedAuthorization rejected) {
            throw new IssuerDeclinedException("Authorization was rejected with status: " + rejected.status() + " and state reason: " + rejected.statusReason());
        }

        // create a new OrderAggregate object with the command object
        OrderAggregate aggregate = new OrderAggregate(command);

        // save the order object in the repository
        aggregate = orderRepository.save(aggregate);

        // return the order object
        return new Order(
            aggregate.getId(),
            aggregate.getVersion(),
            aggregate.getStatus(),
            aggregate.getCurrency(),
            aggregate.getAmount()
        );
    }
}
