package com.example.identity.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminTemplateUploadRequest {

    @NotBlank(message = "Template name is required")
    String name;

    String category;

    String style;

    String thumbnailUrl;

    /**
     * HTML gốc từ Template Builder
     * Chứa placeholders như {{user.name}}, {{#each experiences}}
     */
    @NotBlank(message = "Base HTML is required")
    String baseHtml;

    /**
     * JSON định nghĩa các sections template hỗ trợ
     * Ví dụ: ["header", "summary", "experiences", "education", "skills"]
     */
    JsonNode sectionsDefinition;

    /**
     * Config cho template: colors, fonts, layout...
     */
    JsonNode templateConfig;

    Boolean isPremium;

    /**
     * Admin username (sẽ tự động lấy từ context)
     */
    String createdBy;
}
