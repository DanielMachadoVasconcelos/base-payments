package com.ead.payments.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Converter
@Component
@RequiredArgsConstructor
public class OrderPayloadConverter   implements AttributeConverter<OrderPayload, byte[] > {

    private final ObjectMapper objectMapper;

     @Override
     @SneakyThrows
    public byte[] convertToDatabaseColumn(OrderPayload payload) {
        return objectMapper.writeValueAsBytes(payload);
    }

    @Override
    @SneakyThrows
    public OrderPayload convertToEntityAttribute(byte[] payload) {
        return objectMapper.readValue(payload, OrderPayload.class);
    }
}
