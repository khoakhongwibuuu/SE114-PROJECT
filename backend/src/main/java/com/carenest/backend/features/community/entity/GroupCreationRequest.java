package com.carenest.backend.features.community.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.community.enums.GroupCreationRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.Instant;

@Entity
@Table(name = "group_creation_requests", indexes = {
        @Index(name = "idx_group_creation_requests_status", columnList = "status"),
        @Index(name = "idx_group_creation_requests_requester", columnList = "requester_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreationRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(name = "group_type", nullable = false, length = 40)
    private String groupType;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "short_description", nullable = false, length = 255)
    private String shortDescription;

    @Lob
    @Column(name = "detailed_purpose", nullable = false, columnDefinition = "TEXT")
    private String detailedPurpose;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(name = "moderation_intent", length = 120)
    private String moderationIntent;

    @Lob
    @Column(name = "community_rules", columnDefinition = "TEXT")
    private String communityRules;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupCreationRequestStatus status = GroupCreationRequestStatus.PENDING;

    @Lob
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}
