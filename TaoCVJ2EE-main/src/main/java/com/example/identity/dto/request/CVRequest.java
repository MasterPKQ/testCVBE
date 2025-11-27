package com.example.identity.dto.request;

import com.example.identity.entity.CVSection;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CVRequest {
    Long templateId;
    JsonNode customization;
    String thumbnailUrl;
    String name;
    JsonNode tags;
    Boolean isPublic;
    JsonNode cvData;
    List<CVSection> sections;
}
