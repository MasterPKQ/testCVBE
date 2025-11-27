package com.example.identity.dto.response;

import com.example.identity.entity.CVSection;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CVResponse {
    Long id;
    Long templateId;
    String name;
    JsonNode tags;
    UUID shareToken;
    Boolean isPublic;
    String qrCodeUrl;
    JsonNode cvData;
    JsonNode sectionOrder;
    JsonNode customization;
    String thumbnailUrl;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime lastAccessedAt;

    List<CVSection> sections;
}
