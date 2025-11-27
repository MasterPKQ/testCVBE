package com.example.identity.dto.request;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CVSectionRequestReorder {
    @Id
    Long id;
    int orderIndex;
}
