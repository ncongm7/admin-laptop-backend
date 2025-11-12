package com.example.backendlaptop.converter;

import com.example.backendlaptop.model.TrangThaiHoaDon;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để xử lý enum TrangThaiHoaDon với database
 * Xử lý an toàn các giá trị không hợp lệ trong database
 */
@Converter(autoApply = true)
public class TrangThaiHoaDonConverter implements AttributeConverter<TrangThaiHoaDon, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TrangThaiHoaDon attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.ordinal();
    }

    @Override
    public TrangThaiHoaDon convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        
        // Lấy tất cả giá trị enum
        TrangThaiHoaDon[] values = TrangThaiHoaDon.values();
        
        // Kiểm tra nếu index hợp lệ
        if (dbData >= 0 && dbData < values.length) {
            return values[dbData];
        }
        
        // Nếu giá trị không hợp lệ, log warning và trả về giá trị mặc định
        System.err.println("⚠️ [TrangThaiHoaDonConverter] Giá trị không hợp lệ trong database: " + dbData + 
                          ". Trả về CHO_THANH_TOAN mặc định.");
        
        // Trả về giá trị mặc định hoặc null
        // Có thể thay đổi logic này tùy theo yêu cầu
        return TrangThaiHoaDon.CHO_THANH_TOAN;
    }
}

