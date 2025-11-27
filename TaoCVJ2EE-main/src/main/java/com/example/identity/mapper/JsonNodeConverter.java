package com.example.identity.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false) // (autoApply=true nếu bạn muốn dùng cho MỌI cột JsonNode)
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    // Khởi tạo ObjectMapper, nên dùng DI nếu trong Spring @Component
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        // Biến JsonNode -> Chuỗi JSON
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể chuyển JsonNode thành chuỗi JSON", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        // Biến Chuỗi JSON -> JsonNode
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Đây là hàm parse chính
            return objectMapper.readTree(dbData);
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể parse chuỗi JSON thành JsonNode", e);
        }
    }
}