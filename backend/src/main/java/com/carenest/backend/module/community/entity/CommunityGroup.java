package com.carenest.backend.module.community.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "community_groups", indexes = {
        @Index(name = "idx_community_groups_category", columnList = "category"),
        @Index(name = "idx_community_groups_lead_doctor", columnList = "lead_doctor_id"),
        @Index(name = "idx_community_groups_private", columnList = "is_private")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityGroup extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 80)
    private String category;

    @Column(length = 500)
    private String tags;

    @Builder.Default
    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_doctor_id")
    private User leadDoctor;
}
