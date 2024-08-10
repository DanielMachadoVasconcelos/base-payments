package com.ead.payments.orders;

import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
interface OrdersRepository extends ListPagingAndSortingRepository<OrderEntity, UUID>,
        ListCrudRepository<OrderEntity, UUID> {
}
