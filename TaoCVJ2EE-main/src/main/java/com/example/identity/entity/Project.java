package com.example.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Embedded
    private Duration duration;

    @ElementCollection
    @CollectionTable(name = "project_descriptions", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "description")
    private List<String> description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<Skill> skills;
}
