package com.ead.payments.orders;

import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
record Order(@Id Integer id, Set<LineItem> lineItems) {
}
