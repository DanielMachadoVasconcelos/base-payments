package com.ead.payments.orders;

import org.springframework.data.repository.ListCrudRepository;

interface OrdersRepository extends ListCrudRepository<Order, Integer> {
}
