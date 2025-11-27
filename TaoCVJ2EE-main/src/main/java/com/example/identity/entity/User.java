package com.example.identity.entity;

import com.example.identity.enums.Provider;
import com.example.identity.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    String id;

    @Size(max = 100)
    String email;

    @Size(max = 50)
    @NotNull
    String username;

    @Size(max = 255)
    @NotNull
    String password;

    @Size(max = 50)
    @NotNull
    String firstName;

    @Size(max = 50)
    @NotNull
    String lastName;

    Date dob;

    String avatar;

    @CreationTimestamp
    Date createdAt;

    @UpdateTimestamp
    Date updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    Set<String> roles;


    @Enumerated(EnumType.STRING)
    Provider provider;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<CV> cvs;

    @OneToOne(cascade = CascadeType.ALL)
    Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    UserStatus status = UserStatus.ACTIVE;

}