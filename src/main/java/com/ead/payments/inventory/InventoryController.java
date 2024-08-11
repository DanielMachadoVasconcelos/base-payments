package com.ead.payments.inventory;

import com.ead.payments.orders.OrderPlacedEvent;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
class InventoryController {

    @ApplicationModuleListener
    void on (OrderPlacedEvent event) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(5));  // Simulate inventory update
        log.info("Inventory updated: {}", event.orderId());
    }
}
