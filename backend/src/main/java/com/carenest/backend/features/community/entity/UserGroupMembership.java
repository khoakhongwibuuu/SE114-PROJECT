package com.carenest.backend.features.community.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.community.enums.GroupRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_group_memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_group_membership",
                columnNames = {"user_id", "community_group_id"}
        ),
        indexes = {
                @Index(name = "idx_memberships_user", columnList = "user_id"),
                @Index(name = "idx_memberships_group", columnList = "community_group_id"),
                @Index(name = "idx_memberships_group_role", columnList = "community_group_id, group_role")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupMembership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_group_id", nullable = false)
    private ChatGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_role", nullable = false, length = 20)
    @Builder.Default
    private GroupRole groupRole = GroupRole.MEMBER;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onJoin() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
