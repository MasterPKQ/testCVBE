package com.example.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactInfo {

    @Embedded
    private Location address;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "contact_info_id")
    private List<MediaProfile> mediaProfiles;
}
