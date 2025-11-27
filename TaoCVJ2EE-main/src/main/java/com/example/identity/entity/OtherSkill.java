package com.example.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "other_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
}
