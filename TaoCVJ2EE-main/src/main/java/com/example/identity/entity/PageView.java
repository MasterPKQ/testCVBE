package com.example.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;

@Entity
@Table(name = "page_view")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String page;

    @CreationTimestamp
    private Date createdAt;
}
