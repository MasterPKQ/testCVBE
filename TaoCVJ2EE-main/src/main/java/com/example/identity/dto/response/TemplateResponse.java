package com.example.identity.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TemplateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    Long id;

    String name;

    String category;

    String style;

    String thumbnailUrl;

    @JsonSerialize
    @JsonDeserialize
    JsonNode templateConfig;

    @JsonSerialize
    @JsonDeserialize
    JsonNode sectionsDefinition;

    Boolean isPremium;

    Boolean isActive;

    Integer version;

    String createdBy;

    String compiledFilePath;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
