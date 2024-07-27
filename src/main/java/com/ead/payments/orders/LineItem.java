package com.ead.payments.orders;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_line_items")
public record LineItem(@Id Integer id, Integer productId, int quantity) {
}
