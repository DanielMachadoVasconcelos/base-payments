package com.ead.payments.inventory;

import com.ead.payments.orders.OrderPlacedEvent;
import com.ead.payments.products.ProductCreatedEvent;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Log4j2
@Component
@AllArgsConstructor
class InventoryListener {

    @ApplicationModuleListener
    void on (OrderPlacedEvent event) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(5));  // Simulate inventory update
        log.info("Reserving the Product. Inventory updated: {}", event.toString());
    }

    @ApplicationModuleListener
    void on (ProductCreatedEvent event) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(2));  // Simulate inventory update
        log.info("New Product created. SKU registered: {}", event.toString());
    }
}
