package com.ead.payments.inventory;

import com.ead.payments.orders.OrderPlacedEvent;
import com.ead.payments.products.ProductCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Log4j2
@Component
@AllArgsConstructor
public class InventoryListener {

    @ApplicationModuleListener
    public void on(OrderPlacedEvent event) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(5));  // Simulate inventory update
        log.info("Reserving the Product. Inventory updated: {}", event.toString());
    }

    @ApplicationModuleListener
    public void on(ProductCreatedEvent event) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(5));  // Simulate inventory update
        log.info("New Product created. SKU registered: {}", event.toString());
    }
}
