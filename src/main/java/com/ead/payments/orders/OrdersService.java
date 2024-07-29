package com.ead.payments.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
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

    private final ObjectMapper objectMapper;
    private final OrdersRepository ordersRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Order placeOrder(Order order) throws JsonProcessingException {
        log.info("Placing the Order: {}", order);

        // Convert the request to a byte array. Temporary solution until we have a proper serialization mechanism.
        byte[] bytes = objectMapper.writeValueAsBytes(Map.of("currency", order.currency(), "amount", order.amount()));
        OrderEntity entity = new OrderEntity(order.id(), order.version(),  bytes,  Date.from(Instant.now()), null, null, null);

        OrderEntity saved = ordersRepository.save(entity);
        applicationEventPublisher.publishEvent(new OrderPlacedEvent(saved.id()));

        log.info("Order Placed: {} with version {}", saved.id(), saved.version());
        return new Order(saved.id(), saved.version(), order.currency(), order.amount());
    }
}
