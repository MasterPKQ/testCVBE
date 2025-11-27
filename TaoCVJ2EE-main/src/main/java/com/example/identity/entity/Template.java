package com.example.identity.entity;

import com.example.identity.mapper.JsonNodeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "template")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    String category; // IT, Marketing, Design...

    String style; // professional, creative, minimal, ats-friendly

    @Column(name = "thumbnail_url")
    String thumbnailUrl;

    @Convert(converter = JsonNodeConverter.class)
    @Lob
    @Column(name = "template_config")
    JsonNode templateConfig; // JSON: layout, colors, fonts...

    @Column(name = "is_premium")
    Boolean isPremium;

    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt;

    // ========== NEW FIELDS FOR THYMELEAF SYSTEM ==========

    @Convert(converter = JsonNodeConverter.class)
    @Lob
    @Column(name = "sections_definition")
    JsonNode sectionsDefinition; // Defines which sections this template supports

    @Lob
    @Column(name = "base_html")
    String baseHtml; // Original HTML if uploaded by admin

    @Column(name = "compiled_file_path", length = 500)
    String compiledFilePath; // Path to generated Thymeleaf file

    @Column(name = "version")
    @Builder.Default
    Integer version = 1;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "created_by", length = 100)
    String createdBy; // Admin username

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<CV> cvs;

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;
        Template template = (Template) o;
        return getId() != null && Objects.equals(getId(), template.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
