package com.ead.payments.purchases;

import com.ead.payments.products.ProductCreatedEvent;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class PurchaseController {

    @ApplicationModuleListener
    void on (ProductCreatedEvent event) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(2));  // Simulate inventory update
        log.info("New Product created. Initiating purchase process: {}", event.toString());
    }
}
