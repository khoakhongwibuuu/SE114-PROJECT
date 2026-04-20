package com.carenest.backend.module.family.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.family.enums.FamilyRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "family_members", indexes = {
        @Index(name = "idx_family_members_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_family_members_user", columnNames = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FamilyRole role = FamilyRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private Instant joinedAt = Instant.now();
}
