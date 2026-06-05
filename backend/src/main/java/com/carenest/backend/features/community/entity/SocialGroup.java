package com.carenest.backend.features.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String category;

    @Column
    private String avatarUrl;

    @Column(nullable = false)
    private Long memberCount = 0L;

    @Column(nullable = false)
    private Integer newPostsToday = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (memberCount == null) memberCount = 0L;
        if (newPostsToday == null) newPostsToday = 0;
    }
}
