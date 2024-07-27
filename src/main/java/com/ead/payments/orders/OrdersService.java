package com.ead.payments.orders;

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

    public Order placeOrder(Order order) {
        Order saved = ordersRepository.save(order);
        log.info("Order placed: {}", saved);
        applicationEventPublisher.publishEvent(new OrderPlacedEvent(saved.id()));
        return saved;
    }

}
