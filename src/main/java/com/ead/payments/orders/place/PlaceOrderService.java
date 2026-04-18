package com.ead.payments.orders.place;

import com.ead.payments.orders.Order;
import com.ead.payments.orders.OrderAggregate;
import com.ead.payments.orders.OrderAggregateMapper;
import com.ead.payments.orders.OrderRepository;
import io.micrometer.observation.aop.Cardinality;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.observation.annotation.ObservationKeyValue;
import io.micrometer.observation.annotation.ObservationKeyValues;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ead.payments.orders.place.IssuerService.Authorization;
import static com.ead.payments.orders.place.IssuerService.RejectedAuthorization;

@Service
@RequiredArgsConstructor
public class PlaceOrderService {

    final OrderAggregateMapper orderAggregateMapper;
    final OrderRepository orderRepository;
    final IssuerService issuerService;

    @Observed(
            name = "order-service.place-order",
            contextualName = "place-order-service.handle",
            lowCardinalityKeyValues = {"service", "order-service", "operation", "place-order"}
    )
    public Order handle(
            @ObservationKeyValues({
                    @ObservationKeyValue(key = "currency", expression = "currency.currencyCode", cardinality = Cardinality.LOW),
                    // Let Micrometer stringify the UUID to avoid SpEL method-call issues on record components.
                    @ObservationKeyValue(key = "order.id", expression = "id", cardinality = Cardinality.HIGH)
            })
            PlaceOrderCommand command) {

        // authorize the order
        Authorization authorization = issuerService.authorize(command);

        // if the authorization is rejected, throw an exception
        if (authorization instanceof RejectedAuthorization(String status, String statusReason)) {
            throw new IssuerDeclinedException("Authorization was rejected with status: " + status + " and state reason: " + statusReason);
        }

        // create a new OrderAggregate object with the command object
        OrderAggregate aggregate = new OrderAggregate(command);

        // save the order object in the repository
        aggregate = orderRepository.save(aggregate);

        return orderAggregateMapper.toOrder(aggregate);
    }
}
