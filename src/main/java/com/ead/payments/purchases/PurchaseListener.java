package com.ead.payments.purchases;

import com.ead.payments.products.ProductCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Log4j2
@Component
@AllArgsConstructor
public class PurchaseListener {

    @ApplicationModuleListener
    public void on(ProductCreatedEvent event) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(2));  // Simulate inventory update
        log.info("New Product created. Initiating purchase process: {}", event.toString());
    }
}
