package com.ead.payments.orders.list;

import com.ead.payments.orders.Order.OrderStatus;
import com.ead.payments.orders.OrderAggregate;
import com.ead.payments.orders.OrderAggregateMapper;
import com.ead.payments.orders.OrderRepository;
import com.ead.payments.orders.mapping.LineItemResponseMapper;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderListingService {

    private final OrderRepository orderRepository;
    private final OrderAggregateMapper orderAggregateMapper;
    private final LineItemResponseMapper lineItemResponseMapper;

    @Transactional(readOnly = true)
    public OrderListingResponse findAll(
            OrderStatus status,
            Instant createdFrom,
            Instant createdTo,
            int page,
            int size
    ) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new InvalidOrderListingPeriodException(createdFrom, createdTo);
        }

        var pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Specification<OrderAggregate> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }
        if (createdFrom != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
        }
        if (createdTo != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo));
        }

        var orders = orderRepository.findAll(specification, pageable);
        var content = orders.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new OrderListingResponse(
                content,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages()
        );
    }

    private OrderListingItemResponse toResponse(OrderAggregate aggregate) {
        var order = orderAggregateMapper.toOrder(aggregate);
        return new OrderListingItemResponse(
                aggregate.getId(),
                aggregate.getVersion(),
                aggregate.getCreatedAt(),
                aggregate.getStatus(),
                aggregate.getCurrency(),
                aggregate.getAmount(),
                lineItemResponseMapper.from(order.lineItems())
        );
    }
}
