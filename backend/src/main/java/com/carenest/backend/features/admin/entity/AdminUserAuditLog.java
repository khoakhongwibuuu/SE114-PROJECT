package com.carenest.backend.features.admin.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.admin.enums.AdminUserAuditAction;
import com.carenest.backend.features.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_user_audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserAuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdminUserAuditAction action;

    @Column(name = "previous_role", length = 20)
    private String previousRole;

    @Column(name = "new_role", length = 20)
    private String newRole;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(nullable = false, length = 255)
    private String reason;
}
