package com.example.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "media_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String platform;
    private String handle;
}
