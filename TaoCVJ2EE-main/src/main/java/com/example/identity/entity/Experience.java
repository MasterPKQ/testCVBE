package com.example.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;

    @Embedded
    private Location location;

    @Embedded
    private Duration duration;

    @ElementCollection
    @CollectionTable(name = "experience_descriptions", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "description")
    private List<String> description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "experience_id")
    private List<Skill> skills;
}
