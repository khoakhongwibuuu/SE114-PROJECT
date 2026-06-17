package com.carenest.backend.features.community.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.community.enums.GroupCreationAuditAction;
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

@Entity
@Table(name = "group_creation_audit_logs", indexes = {
        @Index(name = "idx_group_creation_audit_request", columnList = "group_creation_request_id"),
        @Index(name = "idx_group_creation_audit_action", columnList = "action")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreationAuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_creation_request_id", nullable = false)
    private GroupCreationRequest groupCreationRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupCreationAuditAction action;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String note;
}
