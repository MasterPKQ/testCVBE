package com.example.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "educations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String institution;
    private String qualification;

    @Embedded
    private Location location;

    @Embedded
    private Duration duration;

    @ElementCollection
    @CollectionTable(name = "education_descriptions", joinColumns = @JoinColumn(name = "education_id"))
    @Column(name = "description")
    private List<String> description;

    @ElementCollection
    @CollectionTable(name = "education_courses", joinColumns = @JoinColumn(name = "education_id"))
    @Column(name = "course")
    private List<String> courses;
}
