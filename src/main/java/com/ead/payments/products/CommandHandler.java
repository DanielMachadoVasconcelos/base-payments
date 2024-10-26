package com.ead.payments.products;

public interface CommandHandler {
    void handler(CreateProductCommand command);
}
