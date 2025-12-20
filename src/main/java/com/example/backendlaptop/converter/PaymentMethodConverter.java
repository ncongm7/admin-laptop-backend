package com.example.backendlaptop.converter;

import com.example.backendlaptop.model.PaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter cho PaymentMethod enum
 */
@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, String> {
    
    @Override
    public String convertToDatabaseColumn(PaymentMethod attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public PaymentMethod convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return PaymentMethod.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Log warning and return default
            return null;
        }
    }
}
