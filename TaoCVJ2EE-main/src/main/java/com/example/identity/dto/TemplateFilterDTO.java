package com.example.identity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TemplateFilterDTO {
    private String name;
    private String category;
    private String style;
    private Boolean isPremium;
}