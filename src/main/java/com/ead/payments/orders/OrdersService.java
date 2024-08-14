package com.ead.payments.orders;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.weaver.ast.Or;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Order updateOrder(@NotNull UUID orderId, @NotEmpty Long version,  @Min(1) Long amount) {
        return ordersRepository.findById(orderId)
                .map(entity -> entity.withPayload(
                        entity.payload()
                        .withAmount(amount)
                    )
                    .withVersion(version))
                .map(ordersRepository::save)
                .map(orderEntityMapper::from)
                .orElseThrow(() -> new IllegalArgumentException("Resource Order with %s not found".formatted(orderId)));
    }
}
