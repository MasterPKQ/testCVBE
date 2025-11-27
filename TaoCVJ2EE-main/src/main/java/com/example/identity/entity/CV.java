package com.example.identity.entity;

import com.example.identity.mapper.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cv")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // 👇 Liên kết đến User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @ToString.Exclude
    Template template;

    String thumbnailUrl;

    String name; // VD: "CV apply Google"

    @Convert(converter = JsonNodeConverter.class)
    @Lob
    JsonNode tags; // JSON array hoặc table riêng

    @Column(name = "share_token", unique = true)
    UUID shareToken;

    @Column(name = "is_public")
    Boolean isPublic;

    @Column(name = "qr_code_url")
    String qrCodeUrl;

    @Convert(converter = JsonNodeConverter.class)
    @Lob
    @Column(name = "cv_data")
    JsonNode cvData; // chứa toàn bộ nội dung

    @Convert(converter = JsonNodeConverter.class)
    @Lob
    @Column(name = "section_order")
    JsonNode sectionOrder; // thứ tự các sections

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    List<CVSection> sections;

    @Convert(converter = JsonNodeConverter.class)
    @Lob
    @Column(name = "customization")
    JsonNode customization; // colors, fonts, spacing...

    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Column(name = "last_accessed_at")
    LocalDateTime lastAccessedAt;

    public void addSection(CVSection section) {
        if (this.sections == null) {
            this.sections = new java.util.ArrayList<>();
        }
        this.sections.add(section);
        section.setCv(this); // <-- Tự động gán "cha"
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CV cv = (CV) o;
        return getId() != null && Objects.equals(getId(), cv.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
