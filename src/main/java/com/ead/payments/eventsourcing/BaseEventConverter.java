package com.ead.payments.eventsourcing;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Converter(autoApply = true)
public class BaseEventConverter implements AttributeConverter<BaseEvent, String> {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(BaseEvent attribute) {
        return objectMapper.writeValueAsString(attribute);
    }

    @Override
    @SneakyThrows
    public BaseEvent convertToEntityAttribute(String dbData) {
        return objectMapper.readValue(dbData, BaseEvent.class);
    }
}
