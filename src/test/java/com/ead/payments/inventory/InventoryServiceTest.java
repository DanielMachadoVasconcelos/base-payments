package com.ead.payments.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ead.payments.orders.OrderPlacedEvent;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

@ApplicationModuleTest
class InventoryServiceTest {

    @Test
    @DisplayName("Should reduce the product stock when a order is placed")
    void shouldReduceTheProductStockWhenAOrderIsPlaced(Scenario scenario) {

        // Given
        var expectedOrderId = UUID.randomUUID();
        var orderPlacedEvent = new OrderPlacedEvent(expectedOrderId);

        // When
        var result = scenario.publish(orderPlacedEvent);

        // Then
        result.andWaitAtMost(Duration.ofSeconds(5))
                .forEventOfType(OrderPlacedEvent.class)
                .toArriveAndVerify(event ->
                        assertEquals(orderPlacedEvent.orderId(), event.orderId())
                );
    }
}