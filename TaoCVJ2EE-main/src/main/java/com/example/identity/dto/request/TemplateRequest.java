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
public class TemplateRequest {

    @NotBlank(message = "Template name is required")
    String name;

    String category;

    String style;

    String thumbnailUrl;

    JsonNode templateConfig;

    JsonNode sectionsDefinition;

    String baseHtml;

    Boolean isPremium;
}
