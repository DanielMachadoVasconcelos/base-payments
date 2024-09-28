package com.ead.payments.orders;

public interface CommandHandler {
    void handler(PlaceOrderCommand command);
}
