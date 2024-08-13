package com.ead.payments.orders;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderEntityMapper orderEntityMapper;

    public Order placeOrder(Order order)  {
        OrderEntity entity = ordersRepository.save(orderEntityMapper.from(order));
        applicationEventPublisher.publishEvent(new OrderPlacedEvent(entity.id()));
        return orderEntityMapper.from(entity);
    }

    public Optional<Order> findById(UUID orderId) {
        return ordersRepository.findById(orderId)
                .map(orderEntityMapper::from);
    }
}
