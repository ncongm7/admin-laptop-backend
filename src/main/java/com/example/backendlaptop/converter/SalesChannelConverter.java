package com.example.backendlaptop.converter;

import com.example.backendlaptop.model.SalesChannel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter cho SalesChannel enum
 */
@Converter(autoApply = true)
public class SalesChannelConverter implements AttributeConverter<SalesChannel, String> {
    
    @Override
    public String convertToDatabaseColumn(SalesChannel attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public SalesChannel convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return SalesChannel.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Log warning and return default
            return null;
        }
    }
}
